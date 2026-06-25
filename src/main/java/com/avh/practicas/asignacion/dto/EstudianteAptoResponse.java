package com.avh.practicas.asignacion.dto;

import com.avh.practicas.estudiante.entity.Estudiante;

public record EstudianteAptoResponse(
        Long id,
        String identificacion,
        String nombre,
        String correo,
        Long programaId,
        Integer semestre,
        Integer creditosAprobados,
        Double promedioAcumulado
) {
    public static EstudianteAptoResponse desdeEntidad(Estudiante estudiante) {
        return new EstudianteAptoResponse(
                estudiante.getId(),
                estudiante.getIdentificacion(),
                estudiante.getNombre(),
                estudiante.getCorreo(),
                estudiante.getPrograma() != null ? estudiante.getPrograma().getId() : null,
                estudiante.getSemestre(),
                estudiante.getCreditosAprobados(),
                estudiante.getPromedioAcumulado()
        );
    }
}
