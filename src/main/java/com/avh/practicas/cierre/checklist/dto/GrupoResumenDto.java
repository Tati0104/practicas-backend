package com.avh.practicas.cierre.checklist.dto;

import com.avh.practicas.cierre.checklist.EstadoItem;

import java.util.List;

public record GrupoResumenDto(
        String nombre,
        double progreso,
        boolean verificado,
        EstadoItem estado,
        List<ItemResumenDto> items
) {
}
