package com.alertaenchentes.controller;

import com.alertaenchentes.model.Relato;
import com.alertaenchentes.model.RelatoRequest;
import com.alertaenchentes.service.RelatoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/relatos")
@RequiredArgsConstructor
public class RelatoController {

    private final RelatoService relatoService;

    @GetMapping
    public ResponseEntity<List<Relato>> getAllRelatos() {
        return ResponseEntity.ok(relatoService.findAll());
    }

    @PostMapping
    public ResponseEntity<Relato> createRelato(@Valid @RequestBody RelatoRequest request) {
        Relato relato = relatoService.create(
                request.getBairro(),
                request.getTipo(),
                request.getDescricao(),
                request.getFoto(),
                false
        );
        return ResponseEntity.ok(relato);
    }
}