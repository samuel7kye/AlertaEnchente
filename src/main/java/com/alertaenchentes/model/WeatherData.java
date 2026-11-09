package com.alertaenchentes.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherData {

    private Double rainMm;
    private Integer next6hMaxProbability;
    private String disclaimer;
    private LocalDateTime fetchedAt;
}