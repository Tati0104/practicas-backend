package com.avh.practicas.reporte.bridge;

import com.avh.practicas.reporte.builder.ReporteBuilder;
import com.avh.practicas.reporte.builder.ReporteDocumento;
import com.avh.practicas.reporte.dto.FormatoReporte;
import com.avh.practicas.reporte.factory.ReporteExporterFactory;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReporteBuilderExporterFactoryTest {

    @Test
    void reporteBuilder_ConstruyeDocumentoConMetadatosEncabezadosYFilas() {
        ReporteDocumento documento = new ReporteBuilder()
                .titulo("Reporte")
                .metadato("programa", "Ingenieria")
                .encabezados("A", "B")
                .fila("1", "2")
                .build();

        assertEquals("Reporte", documento.getTitulo());
        assertEquals("Ingenieria", documento.getMetadatos().get("programa"));
        assertEquals(2, documento.getEncabezados().size());
        assertEquals("1", documento.getFilas().get(0).get(0));
        assertNotNull(documento.getFechaGeneracion());
    }

    @Test
    void csvReporteExporter_EscapaComillasYValoresNulos() {
        ReporteDocumento documento = ReporteDocumento.builder()
                .titulo("Reporte CSV")
                .fechaGeneracion(LocalDateTime.now())
                .metadatos(Map.of())
                .encabezados(List.of("Nombre", "Nota"))
                .filas(List.of(Arrays.asList("Ana \"A\"", null)))
                .build();

        String csv = new String(new CsvReporteExporter().exportar(documento), StandardCharsets.UTF_8);

        assertTrue(csv.contains("\"Ana \"\"A\"\"\""));
        assertTrue(csv.contains("\"Ana \"\"A\"\"\",\n"));
    }

    @Test
    void csvReporteExporter_RetornaMetadatosDeFormato() {
        CsvReporteExporter exporter = new CsvReporteExporter();

        assertEquals("csv", exporter.extension());
        assertEquals("text/csv", exporter.contentType());
    }

    @Test
    void pdfReporteExporter_GeneraBytesPdf() {
        ReporteDocumento documento = new ReporteBuilder()
                .titulo("Reporte PDF")
                .encabezados("A")
                .fila("1")
                .build();

        byte[] bytes = new PdfReporteExporter().exportar(documento);

        assertTrue(bytes.length > 0);
        assertEquals('%', bytes[0]);
        assertEquals('P', bytes[1]);
    }

    @Test
    void pdfReporteExporter_RetornaMetadatosDeFormato() {
        PdfReporteExporter exporter = new PdfReporteExporter();

        assertEquals("pdf", exporter.extension());
        assertEquals("application/pdf", exporter.contentType());
    }

    @Test
    void reporteExporterFactory_RetornaCsvParaFormatoCsv() {
        CsvReporteExporter csv = new CsvReporteExporter();
        PdfReporteExporter pdf = new PdfReporteExporter();
        ReporteExporterFactory factory = new ReporteExporterFactory(csv, pdf);

        assertSame(csv, factory.crearExporter(FormatoReporte.CSV));
    }

    @Test
    void reporteExporterFactory_RetornaPdfParaFormatoPdf() {
        CsvReporteExporter csv = new CsvReporteExporter();
        PdfReporteExporter pdf = new PdfReporteExporter();
        ReporteExporterFactory factory = new ReporteExporterFactory(csv, pdf);

        assertSame(pdf, factory.crearExporter(FormatoReporte.PDF));
    }
}
