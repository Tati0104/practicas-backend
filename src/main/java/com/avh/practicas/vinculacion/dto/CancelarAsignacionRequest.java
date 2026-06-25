package com.avh.practicas.vinculacion.dto;

import jakarta.validation.constraints.NotBlank;

public record CancelarAsignacionRequest(
        @NotBlank String motivo
) {
}
