package com.alertaenchentes.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

    @Size(max = 260000, message = "Foto excede o tamanho permitido")
    @Pattern(regexp = "^data:image/(jpeg|png|webp|gif);base64,[A-Za-z0-9+/]+={0,2}$", message = "Formato da foto inválido")
    private String foto;
}