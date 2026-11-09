package com.alertaenchentes.service;

import com.alertaenchentes.model.WeatherData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

    private final WebClient webClient;

    @Value("${app.external-apis.open-meteo.base-url}")
    private String baseUrl;

    @Value("${app.external-apis.open-meteo.latitude}")
    private Double latitude;

    @Value("${app.external-apis.open-meteo.longitude}")
    private Double longitude;

    @Value("${app.external-apis.open-meteo.timezone}")
    private String timezone;

    public WeatherData fetchWeather() {
        try {
            Map<String, Object> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/forecast")
                            .queryParam("latitude", latitude)
                            .queryParam("longitude", longitude)
                            .queryParam("timezone", timezone)
                            .queryParam("current_weather", "true")
                            .queryParam("hourly", "precipitation,precipitation_probability")
                            .queryParam("forecast_hours", "6")
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                return getDefaultWeather();
            }

            return parseWeatherResponse(response);
        } catch (Exception e) {
            log.warn("Erro ao buscar dados meteorológicos: {}", e.getMessage());
            return getDefaultWeather();
        }
    }

    private WeatherData parseWeatherResponse(Map<String, Object> response) {
        Double rainMm = 0.0;
        Integer maxProbability = 0;

        if (response.containsKey("current_weather")) {
            Map<String, Object> current = (Map<String, Object>) response.get("current_weather");
            if (current.containsKey("precipitation")) {
                rainMm = ((Number) current.get("precipitation")).doubleValue();
            }
        }

        if (response.containsKey("hourly")) {
            Map<String, Object> hourly = (Map<String, Object>) response.get("hourly");
            if (hourly.containsKey("precipitation_probability")) {
                @SuppressWarnings("unchecked")
                java.util.List<Number> probs = (java.util.List<Number>) hourly.get("precipitation_probability");
                if (!probs.isEmpty()) {
                    maxProbability = probs.stream()
                            .mapToInt(Number::intValue)
                            .max()
                            .orElse(0);
                }
            }
        }

        return WeatherData.builder()
                .rainMm(rainMm)
                .next6hMaxProbability(maxProbability)
                .disclaimer("Leituras meteorológicas são estimativas públicas e não substituem os comunicados oficiais da Defesa Civil.")
                .fetchedAt(LocalDateTime.now())
                .build();
    }

    private WeatherData getDefaultWeather() {
        return WeatherData.builder()
                .rainMm(0.0)
                .next6hMaxProbability(0)
                .disclaimer("Leituras meteorológicas são estimativas públicas e não substituem os comunicados oficiais da Defesa Civil.")
                .fetchedAt(LocalDateTime.now())
                .build();
    }
}