package com.alertaenchentes.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "alertas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "nivel", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private NivelAlerta nivel;

    @Column(name = "bairro", nullable = false, length = 100)
    private String bairro;

    @Column(name = "descricao", nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "ts", nullable = false)
    private LocalDateTime ts;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum NivelAlerta {
        VERDE, AMARELO, VERMELHO
    }
}