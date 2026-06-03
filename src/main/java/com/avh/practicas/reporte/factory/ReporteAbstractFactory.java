package com.avh.practicas.reporte.factory;

import com.avh.practicas.reporte.bridge.ReporteExporter;
import com.avh.practicas.reporte.dto.FormatoReporte;

/**
 * Patron Abstract Factory S6.
 * Define la familia de objetos exportadores segun el formato solicitado.
 */
public interface ReporteAbstractFactory {
    ReporteExporter crearExporter(FormatoReporte formato);
}
