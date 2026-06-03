package com.avh.practicas.calificacion.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para registrar la nota final consolidada de la práctica.
 */
public record NotaFinalRequest(
    @NotNull(message = "La nota final es obligatoria")
    Double notaFinal
) {}
