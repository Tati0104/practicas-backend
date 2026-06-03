package com.avh.practicas.reporte.service;

import com.avh.practicas.asignacion.entity.Asignacion;
import com.avh.practicas.asignacion.entity.EstadoAsignacion;
import com.avh.practicas.asignacion.repository.AsignacionRepository;
import com.avh.practicas.estudiante.repository.EstudianteRepository;
import com.avh.practicas.reporte.bridge.ReporteExporter;
import com.avh.practicas.reporte.builder.ReporteBuilder;
import com.avh.practicas.reporte.builder.ReporteDocumento;
import com.avh.practicas.reporte.dto.ExportacionReporteResponse;
import com.avh.practicas.reporte.dto.ExportarReporteRequest;
import com.avh.practicas.reporte.dto.ReporteResumenDto;
import com.avh.practicas.reporte.dto.TipoReporte;
import com.avh.practicas.reporte.entity.EstadoJobExportacion;
import com.avh.practicas.reporte.entity.JobExportacion;
import com.avh.practicas.reporte.factory.ReporteAbstractFactory;
import com.avh.practicas.reporte.repository.JobExportacionRepository;
import com.avh.practicas.shared.evento.EventoSistema;
import com.avh.practicas.shared.evento.NotificadorEventos;
import com.avh.practicas.shared.evento.TipoEventoSistema;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import com.avh.practicas.vacante.entity.EstadoVacanteEnum;
import com.avh.practicas.vacante.entity.Vacante;
import com.avh.practicas.vacante.repository.VacanteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Servicio S6 para resumen y exportacion de reportes.
 * Combina Builder para armar datos, Abstract Factory para elegir formato y Bridge para exportar.
 */
@Service
@RequiredArgsConstructor
public class ReporteService {

    private final VacanteRepository vacanteRepository;
    private final AsignacionRepository asignacionRepository;
    private final EstudianteRepository estudianteRepository;
    private final JobExportacionRepository jobRepository;
    private final ReporteAbstractFactory reporteFactory;
    private final NotificadorEventos notificadorEventos;

    @Transactional(readOnly = true)
    public ReporteResumenDto resumen() {
        // Consulta de indicadores generales para validar rapidamente el estado del sistema.
        long totalVacantes = vacanteRepository.count();
        long vacantesActivas = vacanteRepository.findAll().stream().filter(v -> v.getEstado() == EstadoVacanteEnum.ACTIVA).count();
        long vacantesPendientes = vacanteRepository.findAll().stream().filter(v -> v.getEstado() == EstadoVacanteEnum.PENDIENTE_APROBACION).count();
        long totalAsignaciones = asignacionRepository.count();
        long asignacionesVinculadas = asignacionRepository.findAll().stream().filter(a -> a.getEstado() == EstadoAsignacion.VINCULADA).count();
        long asignacionesCanceladas = asignacionRepository.findAll().stream().filter(a -> a.getEstado() == EstadoAsignacion.CANCELADA).count();
        long estudiantesRegistrados = estudianteRepository.count();
        return new ReporteResumenDto(totalVacantes, vacantesActivas, vacantesPendientes, totalAsignaciones, asignacionesVinculadas, asignacionesCanceladas, estudiantesRegistrados);
    }

    @Transactional
    public ExportacionReporteResponse exportar(ExportarReporteRequest request) {
        // Se crea un job de exportacion para consultar estado y archivo generado.
        JobExportacion job = jobRepository.save(JobExportacion.builder()
                .tipoReporte(request.tipoReporte().name() + "_" + request.formato().name())
                .estado(EstadoJobExportacion.EN_PROCESO)
                .mensaje("Exportación iniciada")
                .build());

        try {
            // Builder: construye el contenido del reporte segun el tipo solicitado.
            ReporteDocumento documento = construirDocumento(request.tipoReporte());
            // Abstract Factory + Bridge: se obtiene el exportador sin acoplarse a CSV o PDF.
            ReporteExporter exporter = reporteFactory.crearExporter(request.formato());
            byte[] contenido = exporter.exportar(documento);

            Path carpeta = Files.createDirectories(Path.of("exports"));
            String nombre = request.tipoReporte().name().toLowerCase() + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + "." + exporter.extension();
            Path archivo = carpeta.resolve(nombre);
            Files.write(archivo, contenido);

            job.setEstado(EstadoJobExportacion.COMPLETADO);
            job.setUrlArchivo(archivo.toString());
            job.setMensaje("Reporte generado correctamente");
            job = jobRepository.save(job);

            notificadorEventos.notificar(EventoSistema.crear(
                    TipoEventoSistema.REPORTE_GENERADO,
                    request.usuarioId(),
                    "REPORTES",
                    job.getId(),
                    Map.of("tipoReporte", request.tipoReporte().name(), "formato", request.formato().name())
            ));
        } catch (Exception ex) {
            job.setEstado(EstadoJobExportacion.FALLIDO);
            job.setMensaje("No fue posible generar el reporte: " + ex.getMessage());
            job = jobRepository.save(job);
        }

        return ExportacionReporteResponse.desdeEntidad(job);
    }

    @Transactional(readOnly = true)
    public ExportacionReporteResponse estado(Long jobId) {
        return jobRepository.findById(jobId)
                .map(ExportacionReporteResponse::desdeEntidad)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el job de exportación: " + jobId));
    }

    // Selecciona que documento construir segun el tipo de reporte solicitado.
    private ReporteDocumento construirDocumento(TipoReporte tipoReporte) {
        return switch (tipoReporte) {
            case GENERAL -> documentoGeneral();
            case VACANTES -> documentoVacantes();
            case ASIGNACIONES -> documentoAsignaciones();
        };
    }

    private ReporteDocumento documentoGeneral() {
        ReporteResumenDto r = resumen();
        return new ReporteBuilder()
                .titulo("Reporte general de gestión de prácticas")
                .encabezados("Indicador", "Valor")
                .fila("Total vacantes", String.valueOf(r.totalVacantes()))
                .fila("Vacantes activas", String.valueOf(r.vacantesActivas()))
                .fila("Vacantes pendientes", String.valueOf(r.vacantesPendientes()))
                .fila("Total asignaciones", String.valueOf(r.totalAsignaciones()))
                .fila("Asignaciones vinculadas", String.valueOf(r.asignacionesVinculadas()))
                .fila("Asignaciones canceladas", String.valueOf(r.asignacionesCanceladas()))
                .fila("Estudiantes registrados", String.valueOf(r.estudiantesRegistrados()))
                .build();
    }

    // Builder aplicado al reporte de vacantes.
    private ReporteDocumento documentoVacantes() {
        ReporteBuilder builder = new ReporteBuilder()
                .titulo("Reporte de vacantes")
                .encabezados("ID", "Empresa", "Programa", "Cargo", "Estado", "Cupos disponibles");
        for (Vacante v : vacanteRepository.findAll()) {
            builder.fila(
                    String.valueOf(v.getId()),
                    String.valueOf(v.getEmpresaId()),
                    String.valueOf(v.getProgramaId()),
                    v.getCargo(),
                    v.getEstado() != null ? v.getEstado().name() : "",
                    String.valueOf(v.getCuposDisponibles())
            );
        }
        return builder.build();
    }

    // Builder aplicado al reporte de asignaciones/postulaciones.
    private ReporteDocumento documentoAsignaciones() {
        ReporteBuilder builder = new ReporteBuilder()
                .titulo("Reporte de asignaciones/postulaciones")
                .encabezados("ID", "Estudiante", "Vacante", "Coordinador", "Estado", "Fecha creación");
        for (Asignacion a : asignacionRepository.findAll()) {
            builder.fila(
                    String.valueOf(a.getId()),
                    String.valueOf(a.getEstudianteId()),
                    String.valueOf(a.getVacanteId()),
                    String.valueOf(a.getCoordinadorId()),
                    a.getEstado() != null ? a.getEstado().name() : "",
                    String.valueOf(a.getFechaCreacion())
            );
        }
        return builder.build();
    }
}
