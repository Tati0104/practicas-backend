package com.avh.practicas.reporte.builder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Patron Builder S6.
 * Permite construir ReporteDocumento paso a paso: titulo, metadatos, encabezados y filas.
 */
public class ReporteBuilder {

    private String titulo;
    private LocalDateTime fechaGeneracion = LocalDateTime.now();
    private final Map<String, Object> metadatos = new LinkedHashMap<>();
    private final List<String> encabezados = new ArrayList<>();
    private final List<List<String>> filas = new ArrayList<>();

    public ReporteBuilder titulo(String titulo) {
        this.titulo = titulo;
        return this;
    }

    public ReporteBuilder metadato(String clave, Object valor) {
        this.metadatos.put(clave, valor);
        return this;
    }

    public ReporteBuilder encabezados(String... encabezados) {
        this.encabezados.addAll(List.of(encabezados));
        return this;
    }

    public ReporteBuilder fila(String... valores) {
        this.filas.add(List.of(valores));
        return this;
    }

    public ReporteDocumento build() {
        // build(): arma el objeto final inmutable que luego exporta el Bridge.
        return ReporteDocumento.builder()
                .titulo(titulo)
                .fechaGeneracion(fechaGeneracion)
                .metadatos(metadatos)
                .encabezados(encabezados)
                .filas(filas)
                .build();
    }
}
