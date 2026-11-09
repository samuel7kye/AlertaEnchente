package com.alertaenchentes.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvisoRequest {

    @NotNull(message = "Nível é obrigatório")
    private Alerta.NivelAlerta nivel;

    @NotBlank(message = "Bairro é obrigatório")
    @Size(max = 100)
    private String bairro;

    @NotBlank(message = "Mensagem é obrigatória")
    @Size(max = 400, message = "Mensagem deve ter no máximo 400 caracteres")
    private String descricao;
}