package com.avh.practicas.vacante.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record VacanteRequest(
        @NotNull Long empresaId,
        @NotNull Long programaId,
        Long catalogoPracticaId,
        Long creadoPorId,
        String correoEmpresa,
        @NotBlank String cargo,
        @NotBlank String descripcionPerfil,
        String requisitos,
        @NotNull @Min(1) Integer cuposTotales,
        String area,
        @NotBlank String modalidad,
        LocalDate fechaInicioDisponibilidad,
        LocalDate fechaFinDisponibilidad
) {
    public VacanteRequest(
            Long empresaId,
            Long programaId,
            Long creadoPorId,
            String correoEmpresa,
            String cargo,
            String descripcionPerfil,
            String requisitos,
            Integer cuposTotales,
            String area,
            String modalidad,
            LocalDate fechaInicioDisponibilidad,
            LocalDate fechaFinDisponibilidad
    ) {
        this(empresaId, programaId, null, creadoPorId, correoEmpresa, cargo, descripcionPerfil,
                requisitos, cuposTotales, area, modalidad, fechaInicioDisponibilidad, fechaFinDisponibilidad);
    }
}
