package com.avh.practicas.estudiante.dto;

import com.avh.practicas.estudiante.entity.DocenteAsesor;

public record DocenteAsesorResponse(
        Long id,
        Long usuarioId,
        String nombreCompleto,
        String correo,
        Long programaId,
        String areaConocimiento,
        Boolean activo
) {
    public static DocenteAsesorResponse desdeEntidad(DocenteAsesor docente) {
        return new DocenteAsesorResponse(
                docente.getId(),
                docente.getUsuario() == null ? null : docente.getUsuario().getId(),
                docente.getNombreCompleto(),
                docente.getCorreo(),
                docente.getProgramaId(),
                docente.getAreaConocimiento(),
                docente.getActivo()
        );
    }
}
