package com.alertaenchentes.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pontos_monitoramento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PontoMonitoramento {

    @Id
    @Column(name = "id", length = 50)
    private String id;

    @Column(name = "bairro", nullable = false, length = 100)
    private String bairro;

    @Column(name = "ponto", length = 200)
    private String ponto;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusPonto status = StatusPonto.NORMAL;

    @Column(name = "ts")
    private LocalDateTime ts;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum StatusPonto {
        NORMAL, ATENCAO, VERMELHO
    }
}