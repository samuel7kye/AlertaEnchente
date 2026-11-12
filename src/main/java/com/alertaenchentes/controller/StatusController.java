package com.alertaenchentes.controller;

import com.alertaenchentes.model.StatusResponse;
import com.alertaenchentes.service.StatusService;
import com.alertaenchentes.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StatusController {

    private final StatusService statusService;
    private final WeatherService weatherService;

    @GetMapping("/status")
    public ResponseEntity<StatusResponse> getStatus() {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(statusService.getFullStatus());
    }

    @GetMapping("/weather")
    public ResponseEntity<?> getWeather() {
        var weather = weatherService.latestWeather();
        if (weather.isEmpty()) {
            return ResponseEntity.status(503).cacheControl(CacheControl.noStore())
                    .body(Map.of("error", "A primeira leitura meteorológica ainda não está disponível."));
        }
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(weather.get());
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
