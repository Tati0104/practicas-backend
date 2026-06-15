package com.avh.practicas.seguimiento.dto;

import com.avh.practicas.estudiante.dto.EstudianteDto;
import java.time.LocalDate;
import java.util.List;

public record PracticaDetalleResponse(
        Long id,
        EstudianteDto estudiante,
        String empresa,
        String cargo,
        String docente,
        String tutor,
        String estado,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        Integer porcentajeAvance,
        List<TimelineEventDto> timeline
) {}
