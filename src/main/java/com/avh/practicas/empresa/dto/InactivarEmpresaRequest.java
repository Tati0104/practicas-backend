package com.avh.practicas.empresa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InactivarEmpresaRequest(
        @NotBlank(message = "El comentario de inactivación es obligatorio")
        @Size(max = 500, message = "El comentario no puede exceder 500 caracteres")
        String motivo
) {
}
