package com.alertaenchentes.service;

import com.alertaenchentes.model.Relato;
import com.alertaenchentes.repository.RelatoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RelatoService {

    private final RelatoRepository repository;

    public List<Relato> findAll() {
        return repository.findAllByOrderByTsDesc();
    }

    public List<Relato> findByBairro(String bairro) {
        return repository.findByBairroOrderByTsDesc(bairro);
    }

    public Page<Relato> findAllPaginated(Pageable pageable) {
        return repository.findAllByOrderByTsDesc(pageable);
    }

    public List<Relato> findLocalOnly() {
        return repository.findByLocalOnlyTrueOrderByTsDesc();
    }

    @Transactional
    public Relato save(Relato relato) {
        if (relato.getTs() == null) {
            relato.setTs(LocalDateTime.now());
        }
        return repository.save(relato);
    }

    @Transactional
    public Relato create(String bairro, String tipo, String descricao, String foto, boolean localOnly) {
        Relato relato = Relato.builder()
                .bairro(bairro)
                .tipo(tipo)
                .descricao(descricao)
                .foto(foto)
                .ts(LocalDateTime.now())
                .localOnly(localOnly)
                .build();
        return repository.save(relato);
    }
}