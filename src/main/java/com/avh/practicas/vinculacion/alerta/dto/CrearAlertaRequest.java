package com.avh.practicas.vinculacion.alerta.dto;

import jakarta.validation.constraints.NotBlank;

public record CrearAlertaRequest(
        @NotBlank String mensaje,
        boolean prioritaria,
        String urlAccion,
        String nombreModulo,
        String condicionResolucion
) {
}
