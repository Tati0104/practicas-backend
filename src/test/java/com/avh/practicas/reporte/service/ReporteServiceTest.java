package com.avh.practicas.reporte.service;

import com.avh.practicas.asignacion.entity.Asignacion;
import com.avh.practicas.asignacion.entity.EstadoAsignacion;
import com.avh.practicas.asignacion.repository.AsignacionRepository;
import com.avh.practicas.estudiante.repository.EstudianteRepository;
import com.avh.practicas.reporte.bridge.ReporteExporter;
import com.avh.practicas.reporte.builder.ReporteDocumento;
import com.avh.practicas.reporte.dto.ExportacionReporteResponse;
import com.avh.practicas.reporte.dto.ExportarReporteRequest;
import com.avh.practicas.reporte.dto.FormatoReporte;
import com.avh.practicas.reporte.dto.ReporteResumenDto;
import com.avh.practicas.reporte.dto.TipoReporte;
import com.avh.practicas.reporte.entity.EstadoJobExportacion;
import com.avh.practicas.reporte.entity.JobExportacion;
import com.avh.practicas.reporte.factory.ReporteAbstractFactory;
import com.avh.practicas.reporte.repository.JobExportacionRepository;
import com.avh.practicas.shared.evento.EventoSistema;
import com.avh.practicas.shared.evento.NotificadorEventos;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import com.avh.practicas.vacante.entity.EstadoVacanteEnum;
import com.avh.practicas.vacante.entity.Vacante;
import com.avh.practicas.vacante.repository.VacanteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private VacanteRepository vacanteRepository;
    @Mock
    private AsignacionRepository asignacionRepository;
    @Mock
    private EstudianteRepository estudianteRepository;
    @Mock
    private JobExportacionRepository jobRepository;
    @Mock
    private ReporteAbstractFactory reporteFactory;
    @Mock
    private NotificadorEventos notificadorEventos;
    @Mock
    private ReporteExporter exporter;

    private ReporteService service;

    @BeforeEach
    void setUp() {
        service = new ReporteService(
                vacanteRepository,
                asignacionRepository,
                estudianteRepository,
                jobRepository,
                reporteFactory,
                notificadorEventos
        );
    }

    @Test
    void resumen_CuentaVacantesAsignacionesYEstudiantes() {
        when(vacanteRepository.count()).thenReturn(3L);
        when(vacanteRepository.findAll()).thenReturn(List.of(
                vacante(1L, EstadoVacanteEnum.ACTIVA),
                vacante(2L, EstadoVacanteEnum.PENDIENTE_APROBACION),
                vacante(3L, EstadoVacanteEnum.CERRADA)
        ));
        when(asignacionRepository.count()).thenReturn(3L);
        when(asignacionRepository.findAll()).thenReturn(List.of(
                asignacion(1L, EstadoAsignacion.VINCULADA),
                asignacion(2L, EstadoAsignacion.CANCELADA),
                asignacion(3L, EstadoAsignacion.ASIGNADA)
        ));
        when(estudianteRepository.count()).thenReturn(9L);

        ReporteResumenDto result = service.resumen();

        assertEquals(3L, result.totalVacantes());
        assertEquals(1L, result.vacantesActivas());
        assertEquals(1L, result.vacantesPendientes());
        assertEquals(3L, result.totalAsignaciones());
        assertEquals(1L, result.asignacionesVinculadas());
        assertEquals(1L, result.asignacionesCanceladas());
        assertEquals(9L, result.estudiantesRegistrados());
    }

    @Test
    void estado_ExitosoMapeaJob() {
        JobExportacion job = JobExportacion.builder()
                .id(12L)
                .tipoReporte("GENERAL_CSV")
                .estado(EstadoJobExportacion.COMPLETADO)
                .urlArchivo("exports/general.csv")
                .mensaje("ok")
                .fechaCreacion(LocalDateTime.now())
                .build();
        when(jobRepository.findById(12L)).thenReturn(Optional.of(job));

        ExportacionReporteResponse result = service.estado(12L);

        assertEquals(12L, result.jobId());
        assertEquals(EstadoJobExportacion.COMPLETADO, result.estado());
        assertEquals("exports/general.csv", result.urlArchivo());
    }

    @Test
    void estado_FallaSiJobNoExiste() {
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.estado(99L));
    }

    @Test
    void exportar_GeneralCsvMarcaCompletadoYNotifica() {
        when(jobRepository.save(any(JobExportacion.class))).thenAnswer(invocation -> guardarJob(invocation.getArgument(0)));
        when(vacanteRepository.count()).thenReturn(1L);
        when(vacanteRepository.findAll()).thenReturn(List.of(vacante(1L, EstadoVacanteEnum.ACTIVA)));
        when(asignacionRepository.count()).thenReturn(1L);
        when(asignacionRepository.findAll()).thenReturn(List.of(asignacion(1L, EstadoAsignacion.VINCULADA)));
        when(estudianteRepository.count()).thenReturn(2L);
        when(reporteFactory.crearExporter(FormatoReporte.CSV)).thenReturn(exporter);
        when(exporter.exportar(any(ReporteDocumento.class))).thenReturn("contenido".getBytes(StandardCharsets.UTF_8));
        when(exporter.extension()).thenReturn("csv");

        ExportacionReporteResponse result = service.exportar(new ExportarReporteRequest(TipoReporte.GENERAL, FormatoReporte.CSV, 7L));

        assertEquals(EstadoJobExportacion.COMPLETADO, result.estado());
        assertTrue(result.urlArchivo().endsWith(".csv"));
        verify(notificadorEventos).notificar(any(EventoSistema.class));
        new File(result.urlArchivo()).delete();
    }

    @Test
    void exportar_VacantesConstruyeDocumentoConFilas() {
        when(jobRepository.save(any(JobExportacion.class))).thenAnswer(invocation -> guardarJob(invocation.getArgument(0)));
        when(vacanteRepository.findAll()).thenReturn(List.of(vacante(5L, EstadoVacanteEnum.PAUSADA)));
        when(reporteFactory.crearExporter(FormatoReporte.CSV)).thenReturn(exporter);
        when(exporter.exportar(any(ReporteDocumento.class))).thenReturn("csv".getBytes(StandardCharsets.UTF_8));
        when(exporter.extension()).thenReturn("csv");
        ArgumentCaptor<ReporteDocumento> captor = ArgumentCaptor.forClass(ReporteDocumento.class);

        ExportacionReporteResponse result = service.exportar(new ExportarReporteRequest(TipoReporte.VACANTES, FormatoReporte.CSV, 7L));

        verify(exporter).exportar(captor.capture());
        assertEquals("Reporte de vacantes", captor.getValue().getTitulo());
        assertEquals("5", captor.getValue().getFilas().get(0).get(0));
        assertEquals(EstadoJobExportacion.COMPLETADO, result.estado());
        new File(result.urlArchivo()).delete();
    }

    @Test
    void exportar_AsignacionesCuandoExporterFallaMarcaFallido() {
        when(jobRepository.save(any(JobExportacion.class))).thenAnswer(invocation -> guardarJob(invocation.getArgument(0)));
        when(asignacionRepository.findAll()).thenReturn(List.of(asignacion(4L, EstadoAsignacion.CANCELADA)));
        when(reporteFactory.crearExporter(FormatoReporte.CSV)).thenReturn(exporter);
        when(exporter.exportar(any(ReporteDocumento.class))).thenThrow(new IllegalStateException("sin disco"));

        ExportacionReporteResponse result = service.exportar(new ExportarReporteRequest(TipoReporte.ASIGNACIONES, FormatoReporte.CSV, 7L));

        assertEquals(EstadoJobExportacion.FALLIDO, result.estado());
        assertTrue(result.mensaje().contains("sin disco"));
        verifyNoInteractions(notificadorEventos);
    }

    private JobExportacion guardarJob(JobExportacion job) {
        if (job.getId() == null) {
            job.setId(100L);
        }
        if (job.getFechaCreacion() == null) {
            job.setFechaCreacion(LocalDateTime.now());
        }
        return job;
    }

    private Vacante vacante(Long id, EstadoVacanteEnum estado) {
        return Vacante.builder()
                .id(id)
                .empresaId(10L)
                .programaId(20L)
                .cargo("Practicante")
                .estado(estado)
                .cuposDisponibles(2)
                .build();
    }

    private Asignacion asignacion(Long id, EstadoAsignacion estado) {
        return Asignacion.builder()
                .id(id)
                .estudianteId(30L)
                .vacanteId(40L)
                .coordinadorId(50L)
                .estado(estado)
                .fechaCreacion(LocalDateTime.of(2026, 1, 2, 3, 4))
                .build();
    }
}
