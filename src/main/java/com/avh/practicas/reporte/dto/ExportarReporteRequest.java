package com.avh.practicas.reporte.dto;

import jakarta.validation.constraints.NotNull;

public record ExportarReporteRequest(
        @NotNull TipoReporte tipoReporte,
        @NotNull FormatoReporte formato,
        Long usuarioId
) {
}
