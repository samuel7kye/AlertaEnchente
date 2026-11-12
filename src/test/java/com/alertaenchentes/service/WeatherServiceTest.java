package com.alertaenchentes.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeatherServiceTest {

    private static final ZoneId ITAJUBA = ZoneId.of("America/Sao_Paulo");
    private static final Instant FETCHED = Instant.parse("2026-11-13T02:46:00Z");
    private static final String FORECAST = """
            {
              "current": { "time": "2026-11-12T23:45", "precipitation": 2.4 },
              "hourly": {
                "time": ["2026-11-12T23:00", "2026-11-13T00:00", "2026-11-13T01:00",
                         "2026-11-13T02:00", "2026-11-13T03:00", "2026-11-13T04:00", "2026-11-13T05:00"],
                "precipitation_probability": [100, 20, 41, 72, 40, 35, 10]
              }
            }
            """;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void usesSixFutureHoursAcrossMidnight() throws Exception {
        var reading = WeatherService.parseReading(mapper.readTree(FORECAST), ITAJUBA, FETCHED);

        assertThat(reading.getObservedAt()).isEqualTo(Instant.parse("2026-11-13T02:45:00Z"));
        assertThat(reading.getFetchedAt()).isEqualTo(FETCHED);
        assertThat(reading.getRainMm()).isEqualTo(2.4);
        assertThat(reading.getNext6hMaxProbability()).isEqualTo(72);
    }

    @Test
    void rejectsMissingCurrentPrecipitationInsteadOfSavingZero() throws Exception {
        var payload = mapper.readTree(FORECAST.replace("2.4", "null"));

        assertThatThrownBy(() -> WeatherService.parseReading(payload, ITAJUBA, FETCHED))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsMissingForecastHour() throws Exception {
        var payload = mapper.readTree(FORECAST.replace("72", "null"));

        assertThatThrownBy(() -> WeatherService.parseReading(payload, ITAJUBA, FETCHED))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
