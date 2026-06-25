package com.avh.practicas.asignacion.dto;

import jakarta.validation.constraints.NotNull;

public record AsignacionRequest(
        @NotNull Long coordinadorId,
        @NotNull Long estudianteId,
        @NotNull Long vacanteId,
        String notaJustificacion
) {
}
