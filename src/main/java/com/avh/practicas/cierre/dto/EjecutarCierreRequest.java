package com.avh.practicas.cierre.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record EjecutarCierreRequest(
        @NotNull Boolean confirmacion
) {
    @AssertTrue(message = "Debe confirmar el cierre de la práctica")
    public boolean isConfirmacionValida() {
        return Boolean.TRUE.equals(confirmacion);
    }
}
