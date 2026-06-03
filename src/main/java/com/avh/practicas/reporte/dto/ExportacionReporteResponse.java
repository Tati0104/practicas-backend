package com.avh.practicas.reporte.dto;

import com.avh.practicas.reporte.entity.EstadoJobExportacion;
import com.avh.practicas.reporte.entity.JobExportacion;

import java.time.LocalDateTime;

public record ExportacionReporteResponse(
        Long jobId,
        String tipoReporte,
        EstadoJobExportacion estado,
        String urlArchivo,
        String mensaje,
        LocalDateTime fechaCreacion
) {
    public static ExportacionReporteResponse desdeEntidad(JobExportacion job) {
        return new ExportacionReporteResponse(
                job.getId(),
                job.getTipoReporte(),
                job.getEstado(),
                job.getUrlArchivo(),
                job.getMensaje(),
                job.getFechaCreacion()
        );
    }
}
