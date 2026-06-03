package com.avh.practicas.dashboard.dto;

import java.util.Map;

/**
 * Indicadores gerenciales para Dirección (PE-45).
 */
public record DashboardGerencialDto(
        Map<String, Integer> totalPracticantesActivosPorFacultad,
        double tasaAprobacionGlobal,
        Map<String, Double> tasaAprobacionPorPrograma,
        int empresasActivas,
        double tiempoPromedioGestion,
        int practicasCerradasEnPeriodo,
        String periodo,
        Long facultadId
) {
}
