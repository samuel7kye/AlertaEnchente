package com.alertaenchentes.service;

import com.alertaenchentes.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final PontoMonitoramentoService pontoService;
    private final AlertaService alertaService;
    private final RelatoService relatoService;

    @Value("${app.admin.token}")
    private String adminToken;

    public boolean verifyToken(String token) {
        return !adminToken.isBlank() && token != null && MessageDigest.isEqual(
                adminToken.getBytes(StandardCharsets.UTF_8), token.getBytes(StandardCharsets.UTF_8));
    }

    @Transactional
    public Alerta createAviso(AvisoRequest request) {
        PontoMonitoramento ponto = pontoService.findByBairro(request.getBairro().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bairro não monitorado"));
        Alerta alerta = alertaService.create(ponto.getBairro(), request.getNivel(), request.getDescricao().trim());

        PontoMonitoramento.StatusPonto pontoStatus = switch (request.getNivel()) {
            case VERDE -> PontoMonitoramento.StatusPonto.NORMAL;
            case AMARELO -> PontoMonitoramento.StatusPonto.ATENCAO;
            case VERMELHO -> PontoMonitoramento.StatusPonto.VERMELHO;
        };

        pontoService.updateStatus(ponto.getId(), pontoStatus);

        return alerta;
    }

    @Transactional
    public PontoMonitoramento updatePontoStatus(String pontoId, PontoMonitoramento.StatusPonto status) {
        return pontoService.updateStatus(pontoId, status);
    }

    @Transactional
    public Relato createRelato(RelatoRequest request) {
        return relatoService.create(
                request.getBairro(),
                request.getTipo(),
                request.getDescricao(),
                request.getFoto(),
                false
        );
    }

    public List<PontoMonitoramento> getAllPontos() {
        return pontoService.findAll();
    }
}