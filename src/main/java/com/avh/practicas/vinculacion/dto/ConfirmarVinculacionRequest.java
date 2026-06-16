package com.avh.practicas.vinculacion.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ConfirmarVinculacionRequest(
        @NotNull LocalDate fechaInicio,
        @NotNull LocalDate fechaFin,
        @NotNull Long docenteAsesorId
) {
}
