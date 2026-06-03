package com.avh.practicas.asignacion.dto;

import java.util.List;

public record AsignacionDetalleResponse(
        AsignacionResponse asignacion,
        List<HistorialAsignacionResponse> historial
) {
}
