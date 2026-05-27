package com.avh.practicas.correo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlantillaCorreoRequest(
        @NotBlank String asunto,
        @NotBlank String cuerpo,
        @NotNull Boolean activa
) {
}
