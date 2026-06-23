package com.avh.practicas.dashboard.dto;

import com.avh.practicas.asignacion.entity.EstadoAsignacion;
import com.avh.practicas.estudiante.entity.EstadoPractica;

public record PracticaEstudianteResumenDto(
        Long asignacionId,
        Long practicaId,
        Integer numeroPractica,
        String cargo,
        String empresa,
        EstadoAsignacion estadoAsignacion,
        EstadoPractica estadoPractica,
        int documentosCompletos,
        int documentosTotal,
        boolean convenioFirmadoEstudiante,
        String estadoSeguimiento,
        String estadoEncuesta,
        Double notaFinal,
        boolean practicaFinalizada
) {
}
