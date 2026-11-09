package com.alertaenchentes.repository;

import com.alertaenchentes.model.PontoMonitoramento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PontoMonitoramentoRepository extends JpaRepository<PontoMonitoramento, String> {

    List<PontoMonitoramento> findAllByOrderByBairroAsc();

    Optional<PontoMonitoramento> findByBairroIgnoreCase(String bairro);

    List<PontoMonitoramento> findByStatus(PontoMonitoramento.StatusPonto status);
}