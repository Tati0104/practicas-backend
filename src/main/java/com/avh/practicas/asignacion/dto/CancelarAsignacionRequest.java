package com.avh.practicas.asignacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CancelarAsignacionRequest(
        @NotNull Long responsableId,
        @NotBlank String motivo
) {
}
