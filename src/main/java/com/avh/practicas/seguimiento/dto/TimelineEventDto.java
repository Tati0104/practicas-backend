package com.avh.practicas.seguimiento.dto;

import java.time.LocalDateTime;

public record TimelineEventDto(
        Long id,
        String tipo, // "OBSERVACION", "AVANCE_TUTOR", "BITACORA"
        String autor,
        LocalDateTime fecha,
        String contenido,
        Integer porcentaje // opcional para AVANCE_TUTOR
) {}
