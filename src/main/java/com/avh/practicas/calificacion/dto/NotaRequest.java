package com.avh.practicas.calificacion.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para registrar la nota de docente o tutor por corte.
 */
public record NotaRequest(
    @NotNull(message = "La nota es obligatoria")
    Double nota,
    
    String observaciones
) {}
