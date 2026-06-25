package com.avh.practicas.reporte.backup.pattern.abstractfactory;

import java.util.List;

/**
 * Interfaz de fábrica abstracta para definir la estructura de las hojas del reporte (Abstract Factory Pattern).
 */
public interface FabricaExcel {

    /**
     * Retorna los nombres de las columnas para una hoja determinada.
     */
    List<String> obtenerEncabezados(String nombreHoja);

    /**
     * Retorna la lista de filas (datos) para una hoja determinada.
     */
    List<List<Object>> obtenerDatos(String nombreHoja);
}
