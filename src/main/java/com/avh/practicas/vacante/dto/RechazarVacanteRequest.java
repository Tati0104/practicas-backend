package com.avh.practicas.vacante.dto;

import jakarta.validation.constraints.NotBlank;

public record RechazarVacanteRequest(@NotBlank String motivo) {
}
