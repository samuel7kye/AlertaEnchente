package com.alertaenchentes.model;

import java.time.Instant;

public record WeatherData(
        double rainMm,
        int next6hMaxProbability,
        String disclaimer,
        Instant observedAt,
        Instant fetchedAt,
        boolean stale
) {}
