package com.alertaenchentes.service;

import com.alertaenchentes.model.PontoMonitoramento;
import com.alertaenchentes.repository.PontoMonitoramentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PontoMonitoramentoService {

    private final PontoMonitoramentoRepository repository;

    public List<PontoMonitoramento> findAll() {
        return repository.findAllByOrderByBairroAsc();
    }

    public Optional<PontoMonitoramento> findById(String id) {
        return repository.findById(id);
    }

    public Optional<PontoMonitoramento> findByBairro(String bairro) {
        return repository.findByBairroIgnoreCase(bairro);
    }

    public List<PontoMonitoramento> findByStatus(PontoMonitoramento.StatusPonto status) {
        return repository.findByStatus(status);
    }

    @Transactional
    public PontoMonitoramento save(PontoMonitoramento ponto) {
        return repository.save(ponto);
    }

    @Transactional
    public PontoMonitoramento updateStatus(String id, PontoMonitoramento.StatusPonto status) {
        PontoMonitoramento ponto = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ponto não encontrado: " + id));
        ponto.setStatus(status);
        ponto.setTs(LocalDateTime.now());
        return repository.save(ponto);
    }

    @Transactional
    public void initializeDefaultPontos(List<PontoMonitoramento> defaultPontos) {
        for (PontoMonitoramento ponto : defaultPontos) {
            if (!repository.existsById(ponto.getId())) {
                ponto.setStatus(PontoMonitoramento.StatusPonto.NORMAL);
                ponto.setTs(LocalDateTime.now());
                repository.save(ponto);
            }
        }
    }
}