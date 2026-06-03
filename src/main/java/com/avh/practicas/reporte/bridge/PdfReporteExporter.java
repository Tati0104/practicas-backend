package com.avh.practicas.reporte.bridge;

import com.avh.practicas.reporte.builder.ReporteDocumento;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

/**
 * Implementacion concreta del Bridge para exportar reportes en PDF.
 */
@Component
public class PdfReporteExporter implements ReporteExporter {

    @Override
    public byte[] exportar(ReporteDocumento documento) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfDocument pdf = new PdfDocument(new PdfWriter(out));
            Document doc = new Document(pdf);
            doc.add(new Paragraph(documento.getTitulo()).setBold().setFontSize(16));
            doc.add(new Paragraph("Generado en: " + documento.getFechaGeneracion()));

            if (documento.getEncabezados() != null && !documento.getEncabezados().isEmpty()) {
                Table table = new Table(documento.getEncabezados().size());
                documento.getEncabezados().forEach(h -> table.addHeaderCell(h == null ? "" : h));
                documento.getFilas().forEach(fila -> fila.forEach(valor -> table.addCell(valor == null ? "" : valor)));
                doc.add(table);
            }

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No fue posible exportar el reporte PDF", e);
        }
    }

    @Override
    public String extension() {
        return "pdf";
    }

    @Override
    public String contentType() {
        return "application/pdf";
    }
}
