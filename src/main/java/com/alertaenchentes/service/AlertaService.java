package com.alertaenchentes.service;

import com.alertaenchentes.model.Alerta;
import com.alertaenchentes.repository.AlertaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertaService {

    private final AlertaRepository repository;

    public List<Alerta> findAll() {
        return repository.findAllByOrderByTsDesc();
    }

    public List<Alerta> findByBairro(String bairro) {
        return repository.findByBairroOrderByTsDesc(bairro);
    }

    public List<Alerta> findByNivel(Alerta.NivelAlerta nivel) {
        return repository.findByNivelOrderByTsDesc(nivel);
    }

    public Optional<Alerta> findLatestByBairro(String bairro) {
        return repository.findFirstByBairroOrderByTsDesc(bairro);
    }

    @Transactional
    public Alerta save(Alerta alerta) {
        if (alerta.getTs() == null) {
            alerta.setTs(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")));
        }
        return repository.save(alerta);
    }

    @Transactional
    public Alerta create(String bairro, Alerta.NivelAlerta nivel, String descricao) {
        Alerta alerta = Alerta.builder()
                .bairro(bairro)
                .nivel(nivel)
                .descricao(descricao)
                .ts(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")))
                .build();
        return repository.save(alerta);
    }
}