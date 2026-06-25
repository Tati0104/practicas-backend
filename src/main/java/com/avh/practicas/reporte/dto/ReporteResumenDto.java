package com.avh.practicas.reporte.dto;

public record ReporteResumenDto(
        long totalVacantes,
        long vacantesActivas,
        long vacantesPendientes,
        long totalAsignaciones,
        long asignacionesVinculadas,
        long asignacionesCanceladas,
        long estudiantesRegistrados
) {
}
