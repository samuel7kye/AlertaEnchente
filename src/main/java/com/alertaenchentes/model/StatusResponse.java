package com.alertaenchentes.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusResponse {

    private List<PontoMonitoramento> pontos;
    private List<Alerta> alertas;
    private List<Relato> relatos;
}