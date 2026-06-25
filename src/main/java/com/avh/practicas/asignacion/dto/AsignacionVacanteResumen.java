package com.avh.practicas.asignacion.dto;

import com.avh.practicas.vacante.entity.Vacante;

public record AsignacionVacanteResumen(
        Long id,
        String cargo,
        String empresaNombre,
        String modalidad
) {
    public static AsignacionVacanteResumen desdeEntidad(Vacante vacante, String empresaNombre) {
        if (vacante == null) {
            return null;
        }
        return new AsignacionVacanteResumen(
                vacante.getId(),
                vacante.getCargo(),
                empresaNombre,
                vacante.getModalidad()
        );
    }
}
