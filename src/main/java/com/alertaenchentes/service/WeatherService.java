package com.alertaenchentes.service;

import com.alertaenchentes.model.WeatherData;
import com.alertaenchentes.model.WeatherReading;
import com.alertaenchentes.repository.WeatherReadingRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

    private static final String DISCLAIMER =
            "Estimativa meteorológica automatizada. Não mede o nível dos rios nem substitui alertas oficiais.";

    private final WebClient webClient;
    private final WeatherReadingRepository repository;

    @Value("${app.external-apis.open-meteo.base-url}")
    private String baseUrl;

    @Value("${app.external-apis.open-meteo.latitude}")
    private double latitude;

    @Value("${app.external-apis.open-meteo.longitude}")
    private double longitude;

    @Value("${app.external-apis.open-meteo.timezone}")
    private String timezone;

    @Value("${app.external-apis.open-meteo.refresh-interval}")
    private String refreshInterval;

    @Scheduled(fixedRateString = "${app.external-apis.open-meteo.refresh-interval}")
    public void collectWeather() {
        try {
            var uri = UriComponentsBuilder.fromUriString(baseUrl)
                    .queryParam("latitude", latitude)
                    .queryParam("longitude", longitude)
                    .queryParam("timezone", timezone)
                    .queryParam("current", "precipitation")
                    .queryParam("hourly", "precipitation_probability")
                    .queryParam("forecast_hours", 7)
                    .build()
                    .toUri();

            JsonNode response = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block(Duration.ofSeconds(10));

            repository.save(parseReading(response, ZoneId.of(timezone), Instant.now()));
        } catch (RuntimeException error) {
            log.warn("Falha na coleta meteorológica. A última leitura válida continua disponível: {}", error.getMessage());
        }
    }

    public Optional<WeatherData> latestWeather() {
        Instant staleBefore = Instant.now().minus(Duration.parse(refreshInterval)).minus(Duration.ofMinutes(1));
        return repository.findFirstByOrderByFetchedAtDescIdDesc()
                .map(reading -> new WeatherData(
                        reading.getRainMm(),
                        reading.getNext6hMaxProbability(),
                        DISCLAIMER,
                        reading.getObservedAt(),
                        reading.getFetchedAt(),
                        reading.getFetchedAt().isBefore(staleBefore)
                ));
    }

    static WeatherReading parseReading(JsonNode response, ZoneId zone, Instant fetchedAt) {
        if (response == null) {
            throw new IllegalArgumentException("Resposta vazia da Open-Meteo");
        }

        JsonNode current = response.path("current");
        LocalDateTime observed = LocalDateTime.parse(current.path("time").asText());
        JsonNode rain = current.path("precipitation");
        if (!rain.isNumber() || !Double.isFinite(rain.doubleValue()) || rain.doubleValue() < 0) {
            throw new IllegalArgumentException("Precipitação atual ausente ou inválida");
        }

        JsonNode hours = response.path("hourly").path("time");
        JsonNode probabilities = response.path("hourly").path("precipitation_probability");
        LocalDateTime nextHour = observed.withMinute(0).withSecond(0).withNano(0).plusHours(1);
        int maximumProbability = 0;
        int validHours = 0;

        for (int index = 0; index < hours.size() && validHours < 6; index++) {
            LocalDateTime hour = LocalDateTime.parse(hours.path(index).asText());
            if (hour.isBefore(nextHour)) {
                continue;
            }
            JsonNode probability = probabilities.path(index);
            if (!hour.equals(nextHour) || !probability.isIntegralNumber()
                    || !probability.canConvertToInt() || probability.intValue() < 0 || probability.intValue() > 100) {
                throw new IllegalArgumentException("Previsão horária incompleta ou inválida");
            }
            maximumProbability = Math.max(maximumProbability, probability.intValue());
            nextHour = nextHour.plusHours(1);
            validHours++;
        }

        if (validHours != 6) {
            throw new IllegalArgumentException("Previsão das próximas seis horas incompleta");
        }
        return new WeatherReading(observed.atZone(zone).toInstant(), fetchedAt, rain.doubleValue(), maximumProbability);
    }
}
