package com.avh.practicas.reporte.backup.pattern.bridge;

import java.util.List;

/**
 * Interfaz que define las operaciones de bajo nivel para renderizar un libro de Excel (Bridge Pattern - Implementor).
 */
public interface RenderizadorExcel {

    /**
     * Crea una nueva pestaña/hoja en el libro de trabajo.
     */
    void crearHoja(String nombreHoja);

    /**
     * Escribe una fila de celdas en la hoja especificada.
     */
    void escribirFila(String nombreHoja, int numFila, List<Object> celdas);

    /**
     * Exporta el contenido del libro en un flujo de bytes.
     */
    byte[] exportarBytes();
}
