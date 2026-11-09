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
public class RelatoRequest {

    @Size(max = 100)
    private String bairro;

    @Size(max = 50)
    private String tipo;

    @NotBlank(message = "Descrição é obrigatória")
    @Size(max = 400, message = "Descrição deve ter no máximo 400 caracteres")
    private String descricao;

    private String foto;
}