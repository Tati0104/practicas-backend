package com.avh.practicas.asignacion.dto;

import com.avh.practicas.asignacion.entity.Asignacion;
import com.avh.practicas.asignacion.entity.EstadoAsignacion;

import java.time.LocalDateTime;

public record AsignacionResponse(
        Long id,
        Long estudianteId,
        Long vacanteId,
        Long coordinadorId,
        EstadoAsignacion estado,
        String notaJustificacion,
        String motivoCancelacion,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion,
        LocalDateTime fechaVinculacion,
        AsignacionEstudianteResumen estudiante,
        AsignacionVacanteResumen vacante
) {
    public static AsignacionResponse desdeEntidad(
            Asignacion asignacion,
            AsignacionEstudianteResumen estudiante,
            AsignacionVacanteResumen vacante
    ) {
        return new AsignacionResponse(
                asignacion.getId(),
                asignacion.getEstudianteId(),
                asignacion.getVacanteId(),
                asignacion.getCoordinadorId(),
                asignacion.getEstado(),
                asignacion.getNotaJustificacion(),
                asignacion.getMotivoCancelacion(),
                asignacion.getFechaCreacion(),
                asignacion.getFechaActualizacion(),
                asignacion.getFechaVinculacion(),
                estudiante,
                vacante
        );
    }
}
