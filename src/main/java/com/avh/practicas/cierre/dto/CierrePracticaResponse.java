package com.avh.practicas.cierre.dto;

import com.avh.practicas.estudiante.entity.EstadoPractica;

public record CierrePracticaResponse(
        Long practicaId,
        EstadoPractica estado,
        Double notaFinal,
        boolean aprobada,
        String resultado
) {
}
