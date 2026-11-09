package com.alertaenchentes.repository;

import com.alertaenchentes.model.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AlertaRepository extends JpaRepository<Alerta, UUID> {

    List<Alerta> findAllByOrderByTsDesc();

    @Query("SELECT a FROM Alerta a WHERE a.bairro = :bairro ORDER BY a.ts DESC")
    List<Alerta> findByBairroOrderByTsDesc(String bairro);

    List<Alerta> findByNivelOrderByTsDesc(Alerta.NivelAlerta nivel);

    Optional<Alerta> findFirstByBairroOrderByTsDesc(String bairro);
}