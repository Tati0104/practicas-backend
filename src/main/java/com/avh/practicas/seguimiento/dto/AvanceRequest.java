package com.avh.practicas.seguimiento.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para registrar avances de seguimiento del tutor empresarial.
 */
public record AvanceRequest(
    @NotBlank(message = "El texto del avance es obligatorio")
    String avance,
    
    String logros,
    
    String dificultades
) {}
