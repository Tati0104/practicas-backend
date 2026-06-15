package com.avh.practicas.asignacion.dto;

import com.avh.practicas.estudiante.entity.Estudiante;

public record AsignacionEstudianteResumen(
        Long id,
        String nombre,
        String identificacion,
        String programa
) {
    public static AsignacionEstudianteResumen desdeEntidad(Estudiante estudiante) {
        if (estudiante == null) {
            return null;
        }
        String programaNombre = estudiante.getPrograma() != null
                ? estudiante.getPrograma().getNombre()
                : null;
        return new AsignacionEstudianteResumen(
                estudiante.getId(),
                estudiante.getNombre(),
                estudiante.getIdentificacion(),
                programaNombre
        );
    }
}
