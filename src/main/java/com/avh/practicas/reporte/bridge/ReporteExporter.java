package com.avh.practicas.reporte.bridge;

import com.avh.practicas.reporte.builder.ReporteDocumento;

/**
 * Patron Bridge S6.
 * Esta interfaz separa el contenido del reporte de la forma de exportarlo.
 */
public interface ReporteExporter {
    byte[] exportar(ReporteDocumento documento);
    String extension();
    String contentType();
}
