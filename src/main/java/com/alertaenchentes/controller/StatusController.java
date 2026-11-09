package com.alertaenchentes.controller;

import com.alertaenchentes.model.StatusResponse;
import com.alertaenchentes.model.WeatherData;
import com.alertaenchentes.service.StatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StatusController {

    private final StatusService statusService;

    @GetMapping("/status")
    public ResponseEntity<StatusResponse> getStatus() {
        return ResponseEntity.ok(statusService.getFullStatus());
    }

    @GetMapping("/weather")
    public ResponseEntity<WeatherData> getWeather() {
        return ResponseEntity.ok(statusService.getWeather());
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}