package com.avh.practicas.asignacion.dto;

import com.avh.practicas.asignacion.entity.EstadoAsignacion;
import com.avh.practicas.asignacion.entity.HistorialAsignacion;

import java.time.LocalDateTime;

public record HistorialAsignacionResponse(
        Long id,
        Long asignacionId,
        EstadoAsignacion estadoAnterior,
        EstadoAsignacion estadoNuevo,
        Long responsableId,
        String motivo,
        LocalDateTime fecha
) {
    public static HistorialAsignacionResponse desdeEntidad(HistorialAsignacion historial) {
        return new HistorialAsignacionResponse(
                historial.getId(),
                historial.getAsignacionId(),
                historial.getEstadoAnterior(),
                historial.getEstadoNuevo(),
                historial.getResponsableId(),
                historial.getMotivo(),
                historial.getFecha()
        );
    }
}
