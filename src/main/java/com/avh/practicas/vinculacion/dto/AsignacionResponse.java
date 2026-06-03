package com.avh.practicas.vinculacion.dto;

import com.avh.practicas.vinculacion.entity.Asignacion;
import com.avh.practicas.vinculacion.entity.EstadoAsignacion;

import java.time.LocalDateTime;

public record AsignacionResponse(
        Long id,
        Long vacanteId,
        Long estudianteId,
        Long instanciaPracticaId,
        EstadoAsignacion estado,
        String motivoCancelacion,
        LocalDateTime fechaCreacion
) {
    public static AsignacionResponse desde(Asignacion asignacion) {
        return new AsignacionResponse(
                asignacion.getId(),
                asignacion.getVacanteId(),
                asignacion.getEstudianteId(),
                asignacion.getInstanciaPracticaId(),
                asignacion.getEstado(),
                asignacion.getMotivoCancelacion(),
                asignacion.getFechaCreacion()
        );
    }
}
