package com.avh.practicas.respaldo.dto;

import com.avh.practicas.reporte.entity.EstadoJobExportacion;
import com.avh.practicas.reporte.entity.JobExportacion;

import java.time.LocalDateTime;

public record RespaldoResponse(
        Long jobId,
        String tipoReporte,
        EstadoJobExportacion estado,
        String urlArchivo,
        String mensaje,
        LocalDateTime fechaCreacion
) {
    public static RespaldoResponse desdeEntidad(JobExportacion job) {
        return new RespaldoResponse(
                job.getId(),
                job.getTipoReporte(),
                job.getEstado(),
                job.getUrlArchivo(),
                job.getMensaje(),
                job.getFechaCreacion()
        );
    }
}
