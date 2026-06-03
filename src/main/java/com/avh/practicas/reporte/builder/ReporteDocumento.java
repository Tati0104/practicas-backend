package com.avh.practicas.reporte.builder;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
/**
 * Objeto resultado del Builder.
 * Contiene la informacion generica del reporte, independiente del formato final.
 */
@Builder
public class ReporteDocumento {
    private String titulo;
    private LocalDateTime fechaGeneracion;
    private Map<String, Object> metadatos;
    private List<String> encabezados;
    private List<List<String>> filas;
}
