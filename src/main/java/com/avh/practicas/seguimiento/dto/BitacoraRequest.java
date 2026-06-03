package com.avh.practicas.seguimiento.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para registrar bitácoras de actividades del estudiante.
 */
public record BitacoraRequest(
    @NotBlank(message = "La descripción de la bitácora es obligatoria")
    String descripcion
) {}
