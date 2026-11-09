package com.alertaenchentes.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "relatos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Relato {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "bairro", length = 100)
    private String bairro;

    @Column(name = "tipo", length = 50)
    private String tipo;

    @Column(name = "descricao", nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "foto", columnDefinition = "TEXT")
    private String foto;

    @Column(name = "ts", nullable = false)
    private LocalDateTime ts;

    @Column(name = "local_only")
    private Boolean localOnly = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}