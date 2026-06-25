package com.avh.practicas.dashboard.dto;

import java.util.List;

public record PanelEstudianteDto(
        String nombre,
        String correo,
        String identificacion,
        String programa,
        List<PracticaEstudianteResumenDto> practicas
) {
}
