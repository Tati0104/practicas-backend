package com.avh.practicas.vacante.dto;

import com.avh.practicas.vacante.entity.EstadoVacanteEnum;
import com.avh.practicas.vacante.entity.Vacante;

import java.time.LocalDate;

public record VacanteResponse(
        Long id,
        Long empresaId,
        String empresaNombre,
        Long programaId,
        String programaNombre,
        Long creadoPorId,
        Long aprobadoPorId,
        String cargo,
        String descripcionPerfil,
        String requisitos,
        String modalidad,
        String area,
        Integer cuposTotales,
        Integer cuposOcupados,
        Integer cuposDisponibles,
        EstadoVacanteEnum estado,
        String estadoColor,
        String motivoRechazo,
        LocalDate fechaInicioDisponibilidad,
        LocalDate fechaFinDisponibilidad
) {
    public static VacanteResponse desdeEntidad(
            Vacante vacante,
            String empresaNombre,
            String programaNombre) {
        return new VacanteResponse(
                vacante.getId(),
                vacante.getEmpresaId(),
                empresaNombre,
                vacante.getProgramaId(),
                programaNombre,
                vacante.getCreadoPorId(),
                vacante.getAprobadoPorId(),
                vacante.getCargo(),
                vacante.getDescripcionPerfil(),
                vacante.getRequisitos(),
                vacante.getModalidad(),
                vacante.getArea(),
                vacante.getCuposTotales(),
                vacante.getCuposOcupados(),
                vacante.getCuposDisponibles(),
                vacante.getEstado(),
                color(vacante.getEstado()),
                vacante.getMotivoRechazo(),
                vacante.getFechaInicioDisponibilidad(),
                vacante.getFechaFinDisponibilidad()
        );
    }

    private static String color(EstadoVacanteEnum estado) {
        return switch (estado) {
            case PENDIENTE_APROBACION -> "AMARILLO";
            case ACTIVA -> "VERDE";
            case PAUSADA -> "GRIS";
            case CUPOS_COMPLETOS -> "NARANJA";
            case CERRADA -> "ROJO";
            case RECHAZADA -> "ROJO_OSCURO";
        };
    }
}
