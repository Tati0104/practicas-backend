package com.avh.practicas.vacante.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record VacanteRequest(
        @NotNull Long empresaId,
        @NotNull Long programaId,
        Long creadoPorId,
        @NotBlank String cargo,
        String descripcionPerfil,
        String requisitos,
        String modalidad,
        String area,
        @NotNull @Min(1) Integer cuposTotales,
        LocalDate fechaInicioDisponibilidad,
        LocalDate fechaFinDisponibilidad,
        String correoEmpresa
) {
}
