package com.alertaenchentes.repository;

import com.alertaenchentes.model.Relato;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RelatoRepository extends JpaRepository<Relato, UUID> {

    List<Relato> findAllByOrderByTsDesc();

    @Query("SELECT r FROM Relato r WHERE r.bairro = :bairro ORDER BY r.ts DESC")
    List<Relato> findByBairroOrderByTsDesc(String bairro);

    Page<Relato> findAllByOrderByTsDesc(Pageable pageable);

    List<Relato> findByLocalOnlyTrueOrderByTsDesc();
}