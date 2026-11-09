package com.alertaenchentes.model;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PontoStatusUpdate {

    @NotNull(message = "Status é obrigatório")
    private PontoMonitoramento.StatusPonto status;
}