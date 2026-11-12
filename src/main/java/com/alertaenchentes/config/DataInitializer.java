package com.alertaenchentes.config;

import com.alertaenchentes.model.PontoMonitoramento;
import com.alertaenchentes.service.PontoMonitoramentoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
@ConfigurationProperties(prefix = "app")
public class DataInitializer implements CommandLineRunner {

    private final PontoMonitoramentoService pontoService;

    private List<MonitoringPointConfig> monitoringPoints;

    public void setMonitoringPoints(List<MonitoringPointConfig> monitoringPoints) {
        this.monitoringPoints = monitoringPoints;
    }

    @Override
    public void run(String... args) {
        if (monitoringPoints == null || monitoringPoints.isEmpty()) {
            log.warn("Nenhum ponto de monitoramento configurado");
            return;
        }

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