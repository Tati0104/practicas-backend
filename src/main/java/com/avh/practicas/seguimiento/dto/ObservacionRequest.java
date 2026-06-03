package com.avh.practicas.seguimiento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para registrar o actualizar observaciones del docente asesor.
 */
public record ObservacionRequest(
    @NotBlank(message = "El texto de la observación es obligatorio")
    String observacion,
    
    @NotNull(message = "La visibilidad para el estudiante es obligatoria")
    Boolean visibleParaEstudiante
) {}
