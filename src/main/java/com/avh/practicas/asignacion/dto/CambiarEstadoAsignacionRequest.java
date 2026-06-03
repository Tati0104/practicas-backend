package com.avh.practicas.asignacion.dto;

import jakarta.validation.constraints.NotNull;

public record CambiarEstadoAsignacionRequest(
        @NotNull Long responsableId,
        String motivo
) {
}
