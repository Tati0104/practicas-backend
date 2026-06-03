package com.avh.practicas.reporte.bridge;

import com.avh.practicas.reporte.builder.ReporteDocumento;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Implementacion concreta del Bridge para exportar reportes en CSV.
 */
@Component
public class CsvReporteExporter implements ReporteExporter {

    @Override
    public byte[] exportar(ReporteDocumento documento) {
        StringBuilder csv = new StringBuilder();
        csv.append(documento.getTitulo()).append("\n");
        csv.append("Generado en,").append(documento.getFechaGeneracion()).append("\n\n");
        csv.append(linea(documento.getEncabezados())).append("\n");
        documento.getFilas().forEach(fila -> csv.append(linea(fila)).append("\n"));
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String linea(java.util.List<String> valores) {
        return valores.stream()
                .map(v -> v == null ? "" : '"' + v.replace("\"", "\"\"") + '"')
                .collect(Collectors.joining(","));
    }

    @Override
    public String extension() {
        return "csv";
    }

    @Override
    public String contentType() {
        return "text/csv";
    }
}
