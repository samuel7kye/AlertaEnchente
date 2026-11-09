package com.alertaenchentes.controller;

import com.alertaenchentes.model.*;
import com.alertaenchentes.service.AdminService;
import com.alertaenchentes.service.PontoMonitoramentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final PontoMonitoramentoService pontoService;

    @PostMapping("/session")
    public ResponseEntity<Map<String, Object>> verifySession(@Valid @RequestBody AdminLoginRequest request) {
        boolean valid = adminService.verifyToken(request.getToken());
        if (valid) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Acesso liberado"));
        }
        return ResponseEntity.status(401).body(Map.of("success", false, "error", "Senha incorreta"));
    }

    @PostMapping("/alertas")
    public ResponseEntity<Alerta> createAviso(@Valid @RequestBody AvisoRequest request) {
        Alerta alerta = adminService.createAviso(request);
        List<PontoMonitoramento> pontos = adminService.getAllPontos();
        return ResponseEntity.ok(alerta);
    }

    @PatchMapping("/pontos/{id}")
    public ResponseEntity<PontoMonitoramento> updatePontoStatus(
            @PathVariable String id,
            @Valid @RequestBody PontoStatusUpdate request
    ) {
        PontoMonitoramento ponto = adminService.updatePontoStatus(id, request.getStatus());
        return ResponseEntity.ok(ponto);
    }
}