package com.avh.practicas.respaldo.service;

import com.avh.practicas.reporte.entity.EstadoJobExportacion;
import com.avh.practicas.reporte.entity.JobExportacion;
import com.avh.practicas.reporte.repository.JobExportacionRepository;
import com.avh.practicas.respaldo.dto.RespaldoResponse;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Stub de respaldo solicitado en S5.
 * Genera un Excel basico para validar el flujo asincrono antes del respaldo completo.
 */
@Service
@RequiredArgsConstructor
public class RespaldoServiceStub implements IRespaldoService {

    private final JobExportacionRepository jobRepository;

    @Override
    @Transactional
    public RespaldoResponse generarRespaldo() {
        // Se crea un job en PENDIENTE para poder consultar luego su estado.
        JobExportacion job = jobRepository.save(JobExportacion.builder()
                .tipoReporte("RESPALDO_KBM_STUB")
                .estado(EstadoJobExportacion.PENDIENTE)
                .mensaje("Respaldo solicitado. El archivo se generará de forma asíncrona.")
                .build());
        generarArchivoAsync(job.getId());
        return RespaldoResponse.desdeEntidad(job);
    }

    // @Async: simula procesamiento en segundo plano para no bloquear la peticion HTTP.
    @Async
    @Transactional
    public void generarArchivoAsync(Long jobId) {
        JobExportacion job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el job de respaldo: " + jobId));
        try {
            job.setEstado(EstadoJobExportacion.EN_PROCESO);
            jobRepository.save(job);

            Path carpeta = Files.createDirectories(Path.of("exports"));
            String nombre = "Respaldo_GestionPracticas_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss")) + ".xlsx";
            Path archivo = carpeta.resolve(nombre);

            // Apache POI: se crea el archivo Excel de respaldo basico.
            try (Workbook workbook = new XSSFWorkbook(); OutputStream out = Files.newOutputStream(archivo)) {
                Sheet hoja = workbook.createSheet("Usuarios");
                Row encabezado = hoja.createRow(0);
                encabezado.createCell(0).setCellValue("id");
                encabezado.createCell(1).setCellValue("nombre");
                encabezado.createCell(2).setCellValue("correo");
                encabezado.createCell(3).setCellValue("rol");
                Row ejemplo = hoja.createRow(1);
                ejemplo.createCell(0).setCellValue("stub");
                ejemplo.createCell(1).setCellValue("Respaldo base KBM");
                ejemplo.createCell(2).setCellValue("pendiente de integración completa");
                ejemplo.createCell(3).setCellValue("ADMIN");
                workbook.write(out);
            }

            job.setEstado(EstadoJobExportacion.COMPLETADO);
            job.setUrlArchivo(archivo.toString());
            job.setMensaje("Respaldo generado correctamente");
            jobRepository.save(job);
        } catch (IOException | RuntimeException ex) {
            job.setEstado(EstadoJobExportacion.FALLIDO);
            job.setMensaje("No fue posible generar el respaldo: " + ex.getMessage());
            jobRepository.save(job);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public RespaldoResponse consultarEstado(Long jobId) {
        return jobRepository.findById(jobId)
                .map(RespaldoResponse::desdeEntidad)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el job de respaldo: " + jobId));
    }
}
