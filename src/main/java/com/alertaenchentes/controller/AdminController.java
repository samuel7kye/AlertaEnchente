package com.alertaenchentes.controller;

import com.alertaenchentes.model.*;
import com.alertaenchentes.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/session")
    public ResponseEntity<Map<String, Object>> currentSession(Authentication authentication, CsrfToken csrfToken) {
        boolean authenticated = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(Map.of("authenticated", authenticated, "csrfToken", csrfToken.getToken()));
    }

    @PostMapping("/session")
    public ResponseEntity<Map<String, Object>> verifySession(@Valid @RequestBody AdminLoginRequest request) {
        if (adminService.verifyToken(request.getToken())) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Acesso liberado"));
        }
        return ResponseEntity.status(401).body(Map.of("success", false, "error", "Senha incorreta"));
    }

    @PostMapping("/alertas")
    public ResponseEntity<Alerta> createAviso(@Valid @RequestBody AvisoRequest request) {
        return ResponseEntity.ok(adminService.createAviso(request));
    }

    @PatchMapping("/pontos/{id}")
    public ResponseEntity<PontoMonitoramento> updatePontoStatus(
            @PathVariable String id,
            @Valid @RequestBody PontoStatusUpdate request
    ) {
        return ResponseEntity.ok(adminService.updatePontoStatus(id, request.getStatus()));
    }
}
