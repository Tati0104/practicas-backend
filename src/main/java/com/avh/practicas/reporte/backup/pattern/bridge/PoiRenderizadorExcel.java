package com.avh.practicas.reporte.backup.pattern.bridge;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación concreta del renderizador de Excel usando Apache POI (Bridge Pattern - ConcreteImplementor).
 */
public class PoiRenderizadorExcel implements RenderizadorExcel {

    private final Workbook workbook;
    private final Map<String, Sheet> sheets;

    public PoiRenderizadorExcel() {
        this.workbook = new XSSFWorkbook();
        this.sheets = new HashMap<>();
    }

    @Override
    public void crearHoja(String nombreHoja) {
        Sheet sheet = workbook.createSheet(nombreHoja);
        sheets.put(nombreHoja, sheet);
    }

    @Override
    public void escribirFila(String nombreHoja, int numFila, List<Object> celdas) {
        Sheet sheet = sheets.get(nombreHoja);
        if (sheet == null) {
            throw new IllegalArgumentException("No existe la hoja: " + nombreHoja);
        }

        Row row = sheet.createRow(numFila);
        for (int i = 0; i < celdas.size(); i++) {
            Cell cell = row.createCell(i);
            Object valor = celdas.get(i);
            if (valor == null) {
                cell.setCellValue("");
            } else if (valor instanceof Number) {
                cell.setCellValue(((Number) valor).doubleValue());
            } else if (valor instanceof Boolean) {
                cell.setCellValue((Boolean) valor);
            } else if (valor instanceof LocalDate) {
                cell.setCellValue(((LocalDate) valor).toString());
            } else if (valor instanceof LocalDateTime) {
                cell.setCellValue(((LocalDateTime) valor).toString());
            } else {
                cell.setCellValue(valor.toString());
            }
        }
    }

    @Override
    public byte[] exportarBytes() {
        // Autoajustar ancho de columnas
        for (Sheet sheet : sheets.values()) {
            int maxCol = 0;
            for (Row row : sheet) {
                if (row.getLastCellNum() > maxCol) {
                    maxCol = row.getLastCellNum();
                }
            }
            for (int col = 0; col < maxCol; col++) {
                try {
                    sheet.autoSizeColumn(col);
                } catch (Exception e) {
                    // Ignorar error al autoajustar en ambientes sin UI
                }
            }
        }

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            workbook.write(bos);
            workbook.close();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error escribiendo libro Excel", e);
        }
    }
}
