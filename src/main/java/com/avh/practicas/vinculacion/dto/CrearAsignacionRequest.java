package com.avh.practicas.vinculacion.dto;

import jakarta.validation.constraints.NotNull;

public record CrearAsignacionRequest(
        @NotNull Long vacanteId,
        @NotNull Long estudianteId
) {
}
