package com.alertaenchentes.service;

import com.alertaenchentes.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatusService {

    private final PontoMonitoramentoService pontoService;
    private final AlertaService alertaService;
    private final RelatoService relatoService;

    public StatusResponse getFullStatus() {
        List<PontoMonitoramento> pontos = pontoService.findAll();
        List<Alerta> alertas = alertaService.findAll();
        List<Relato> relatos = relatoService.findAll();

        return StatusResponse.builder()
                .pontos(pontos)
                .alertas(alertas)
                .relatos(relatos)
                .build();
    }
}