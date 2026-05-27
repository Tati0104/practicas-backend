package com.avh.practicas.correo.dto;

import jakarta.validation.constraints.NotBlank;

public record PlantillaCorreoRequest(
        @NotBlank String asunto,
        @NotBlank String cuerpo
) {
}
