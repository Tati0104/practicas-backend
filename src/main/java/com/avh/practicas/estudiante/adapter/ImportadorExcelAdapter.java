package com.avh.practicas.estudiante.adapter;

import com.avh.practicas.configuracion.entity.Programa;
import com.avh.practicas.configuracion.repository.ProgramaRepository;
import com.avh.practicas.estudiante.dto.ErrorValidacion;
import com.avh.practicas.estudiante.dto.ResultadoImportacion;
import com.avh.practicas.estudiante.entity.EstadoAptitud;
import com.avh.practicas.estudiante.entity.Estudiante;
import com.avh.practicas.estudiante.repository.EstudianteRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class ImportadorExcelAdapter implements ImportadorEstudiantes {

    private final EstudianteRepository estudianteRepository;
    private final ProgramaRepository programaRepository;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    @Override
    public ResultadoImportacion importar(byte[] archivoBytes) {
        List<Estudiante> exitosos = new ArrayList<>();
        List<ErrorValidacion> errores = new ArrayList<>();
        int totalProcesados = 0;

        try (ByteArrayInputStream bis = new ByteArrayInputStream(archivoBytes);
             Workbook workbook = new XSSFWorkbook(bis)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getPhysicalNumberOfRows() == 0) {
                return ResultadoImportacion.builder()
                        .exitosos(exitosos)
                        .errores(List.of(new ErrorValidacion(1, "El archivo Excel está completamente vacío.")))
                        .total(0)
                        .build();
            }

            // Leer cabecera para mapear columnas por nombre (Fila 0)
            Row headerRow = sheet.getRow(0);
            Map<String, Integer> headerMap = getHeaderMapping(headerRow);

            // Validar que las columnas obligatorias estén mapeadas
            List<String> columnasObligatorias = List.of("identificacion", "nombre", "correo", "programaid");
            for (String col : columnasObligatorias) {
                if (!headerMap.containsKey(col)) {
                    return ResultadoImportacion.builder()
                            .exitosos(exitosos)
                            .errores(List.of(new ErrorValidacion(1, "Falta la columna obligatoria: '" + col + "' en la cabecera del archivo.")))
                            .total(0)
                            .build();
                }
            }

            int lastRowIndex = sheet.getLastRowNum();
            totalProcesados = lastRowIndex; // El total de filas de datos (excluyendo cabecera)

            // Procesar fila a fila (de la 1 en adelante)
            for (int r = 1; r <= lastRowIndex; r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowEmpty(row)) {
                    continue; // Saltar filas vacías
                }

                int numFilaUsuario = r + 1; // Para reportar al usuario (1-indexed)
                List<String> erroresFila = new ArrayList<>();

                // Obtener valores de celdas
                String identificacion = getCellMappedValue(row, headerMap, "identificacion");
                String nombre = getCellMappedValue(row, headerMap, "nombre");
                String correo = getCellMappedValue(row, headerMap, "correo");
                String telefono = getCellMappedValue(row, headerMap, "telefono");
                String contactoEmergencia = getCellMappedValue(row, headerMap, "contactoemergencia");
                String programaIdStr = getCellMappedValue(row, headerMap, "programaid");
                String semestreStr = getCellMappedValue(row, headerMap, "semestre");
                String creditosStr = getCellMappedValue(row, headerMap, "creditosaprobados");
                String promedioStr = getCellMappedValue(row, headerMap, "promedioacumulado");

                // Validaciones de campos obligatorios
                if (!StringUtils.hasText(identificacion)) {
                    erroresFila.add("La identificación es un campo obligatorio.");
                }
                if (!StringUtils.hasText(nombre)) {
                    erroresFila.add("El nombre es un campo obligatorio.");
                }
                if (!StringUtils.hasText(correo)) {
                    erroresFila.add("El correo es un campo obligatorio.");
                }
                if (!StringUtils.hasText(programaIdStr)) {
                    erroresFila.add("El ID de programa es un campo obligatorio.");
                }

                // Si hay campos obligatorios faltantes, no podemos continuar con validaciones profundas en esta fila
                if (!erroresFila.isEmpty()) {
                    errores.add(new ErrorValidacion(numFilaUsuario, String.join(" | ", erroresFila)));
                    continue;
                }

                // Validar formato de correo electrónico
                if (!EMAIL_PATTERN.matcher(correo).matches()) {
                    erroresFila.add("El formato del correo electrónico es inválido.");
                }

                // Validar duplicados en base de datos
                if (estudianteRepository.existsByIdentificacion(identificacion)) {
                    erroresFila.add("La identificación '" + identificacion + "' ya está registrada en el sistema.");
                }

                // Validar que no se repita en la misma lista importada
                boolean identDuplicadaEnLista = exitosos.stream().anyMatch(e -> e.getIdentificacion().equals(identificacion));
                if (identDuplicadaEnLista) {
                    erroresFila.add("La identificación '" + identificacion + "' está duplicada en el mismo archivo.");
                }

                if (estudianteRepository.existsByCorreo(correo)) {
                    erroresFila.add("El correo '" + correo + "' ya está registrado en el sistema.");
                }
                
                boolean correoDuplicadoEnLista = exitosos.stream().anyMatch(e -> e.getCorreo().equalsIgnoreCase(correo));
                if (correoDuplicadoEnLista) {
                    erroresFila.add("El correo '" + correo + "' está duplicado en el mismo archivo.");
                }

                // Validar existencia de programa
                Programa programa = null;
                try {
                    Long progId = Long.parseLong(programaIdStr);
                    Optional<Programa> progOpt = programaRepository.findById(progId);
                    if (progOpt.isPresent()) {
                        programa = progOpt.get();
                        if (!programa.getActivo()) {
                            erroresFila.add("El programa con ID " + progId + " está inactivo.");
                        }
                    } else {
                        erroresFila.add("No existe el programa con ID " + progId + " en el sistema.");
                    }
                } catch (NumberFormatException e) {
                    erroresFila.add("El ID del programa debe ser un número válido.");
                }

                // Parsear semestre, créditos y promedio acumulado con control de errores
                Integer semestre = null;
                if (StringUtils.hasText(semestreStr)) {
                    try {
                        semestre = Integer.parseInt(semestreStr);
                        if (semestre < 1) {
                            erroresFila.add("El semestre debe ser mayor o igual a 1.");
                        }
                    } catch (NumberFormatException e) {
                        erroresFila.add("El semestre debe ser un número entero válido.");
                    }
                }

                Integer creditosAprobados = 0;
                if (StringUtils.hasText(creditosStr)) {
                    try {
                        creditosAprobados = Integer.parseInt(creditosStr);
                        if (creditosAprobados < 0) {
                            erroresFila.add("Los créditos aprobados no pueden ser negativos.");
                        }
                    } catch (NumberFormatException e) {
                        erroresFila.add("Los créditos aprobados deben ser un número entero válido.");
                    }
                }

                Double promedioAcumulado = 0.0;
                if (StringUtils.hasText(promedioStr)) {
                    try {
                        promedioAcumulado = Double.parseDouble(promedioStr);
                        if (promedioAcumulado < 0.0 || promedioAcumulado > 5.0) {
                            erroresFila.add("El promedio acumulado debe estar entre 0.0 y 5.0.");
                        }
                    } catch (NumberFormatException e) {
                        erroresFila.add("El promedio acumulado debe ser un número decimal válido.");
                    }
                }

                // Consolidar errores de esta fila
                if (!erroresFila.isEmpty()) {
                    errores.add(new ErrorValidacion(numFilaUsuario, String.join(" | ", erroresFila)));
                } else {
                    // Crear entidad Estudiante válida
                    Estudiante estudiante = Estudiante.builder()
                            .identificacion(identificacion)
                            .nombre(nombre)
                            .correo(correo)
                            .telefono(telefono)
                            .contactoEmergencia(contactoEmergencia)
                            .programa(programa)
                            .semestre(semestre)
                            .creditosAprobados(creditosAprobados)
                            .promedioAcumulado(promedioAcumulado)
                            .estadoAptitud(EstadoAptitud.SIN_EVALUAR)
                            .build();

                    exitosos.add(estudiante);
                }
            }

        } catch (Exception e) {
            errores.add(new ErrorValidacion(1, "Error crítico de lectura del archivo: " + e.getMessage()));
        }

        return ResultadoImportacion.builder()
                .exitosos(exitosos)
                .errores(errores)
                .total(totalProcesados)
                .build();
    }

    @Override
    public List<ErrorValidacion> validar(byte[] archivoBytes) {
        return importar(archivoBytes).getErrores();
    }

    @Async
    public CompletableFuture<ResultadoImportacion> importarAsync(byte[] archivoBytes) {
        return CompletableFuture.completedFuture(importar(archivoBytes));
    }

    private Map<String, Integer> getHeaderMapping(Row headerRow) {
        Map<String, Integer> mapping = new HashMap<>();
        if (headerRow != null) {
            for (Cell cell : headerRow) {
                String headerVal = cell.getStringCellValue().trim().toLowerCase().replaceAll("\\s+", "");
                mapping.put(headerVal, cell.getColumnIndex());
            }
        }
        return mapping;
    }

    private String getCellMappedValue(Row row, Map<String, Integer> mapping, String columnName) {
        if (!mapping.containsKey(columnName)) {
            return "";
        }
        int colIndex = mapping.get(columnName);
        Cell cell = row.getCell(colIndex);
        return getCellValueAsString(cell);
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                double num = cell.getNumericCellValue();
                if (num == (long) num) {
                    return String.valueOf((long) num);
                }
                return String.valueOf(num);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }

    private boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK && StringUtils.hasText(getCellValueAsString(cell))) {
                return false;
            }
        }
        return true;
    }
}
