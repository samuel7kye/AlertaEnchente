package com.alertaenchentes.config;

import com.alertaenchentes.model.PontoMonitoramento;
import com.alertaenchentes.service.PontoMonitoramentoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final PontoMonitoramentoService pontoService;

    @Value("${app.monitoring-points}")
    private List<MonitoringPointConfig> monitoringPoints;

    @Override
    public void run(String... args) {
        List<PontoMonitoramento> defaultPontos = monitoringPoints.stream()
                .map(config -> PontoMonitoramento.builder()
                        .id(config.id())
                        .bairro(config.bairro())
                        .ponto(config.ponto())
                        .latitude(config.latitude())
                        .longitude(config.longitude())
                        .status(PontoMonitoramento.StatusPonto.NORMAL)
                        .build())
                .collect(Collectors.toList());

        pontoService.initializeDefaultPontos(defaultPontos);
        log.info("Pontos de monitoramento inicializados: {}", defaultPontos.size());
    }

    public record MonitoringPointConfig(
            String id,
            String bairro,
            String ponto,
            Double latitude,
            Double longitude
    ) {}
}