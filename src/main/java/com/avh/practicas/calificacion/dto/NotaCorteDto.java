package com.avh.practicas.calificacion.dto;

/**
 * DTO auxiliar que representa el detalle de notas de un corte.
 */
public record NotaCorteDto(
    Integer corte,
    Double notaDocente,
    String obsDocente,
    Double notaTutor,
    String obsTutor
) {}
