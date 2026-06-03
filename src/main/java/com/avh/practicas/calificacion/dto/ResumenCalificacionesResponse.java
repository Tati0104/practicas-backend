package com.avh.practicas.calificacion.dto;

import java.util.List;

/**
 * DTO que resume el estado académico completo de una práctica.
 */
public record ResumenCalificacionesResponse(
    Long practicaId,
    List<NotaCorteDto> notasCortes,
    Double notaFinal,
    Boolean aprobada,
    Double promedioEstimado
) {}
