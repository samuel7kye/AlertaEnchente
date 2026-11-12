package com.alertaenchentes.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "weather_readings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeatherReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant observedAt;

    @Column(nullable = false)
    private Instant fetchedAt;

    @Column(nullable = false)
    private double rainMm;

    @Column(name = "next_6h_max_probability", nullable = false)
    private int next6hMaxProbability;

    public WeatherReading(Instant observedAt, Instant fetchedAt, double rainMm, int next6hMaxProbability) {
        this.observedAt = observedAt;
        this.fetchedAt = fetchedAt;
        this.rainMm = rainMm;
        this.next6hMaxProbability = next6hMaxProbability;
    }
}
