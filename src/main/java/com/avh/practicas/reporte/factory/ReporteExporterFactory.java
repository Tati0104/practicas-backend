package com.avh.practicas.reporte.factory;

import com.avh.practicas.reporte.bridge.CsvReporteExporter;
import com.avh.practicas.reporte.bridge.PdfReporteExporter;
import com.avh.practicas.reporte.bridge.ReporteExporter;
import com.avh.practicas.reporte.dto.FormatoReporte;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Fabrica concreta de exportadores.
 * Decide si el reporte se genera en CSV o PDF sin acoplar ReporteService a una clase especifica.
 */
@Component
@RequiredArgsConstructor
public class ReporteExporterFactory implements ReporteAbstractFactory {

    private final CsvReporteExporter csvExporter;
    private final PdfReporteExporter pdfExporter;

    @Override
    public ReporteExporter crearExporter(FormatoReporte formato) {
        // Abstract Factory: se selecciona la implementacion concreta por parametro de formato.
        return switch (formato) {
            case CSV -> csvExporter;
            case PDF -> pdfExporter;
        };
    }
}
