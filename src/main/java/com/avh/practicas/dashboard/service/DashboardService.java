package com.avh.practicas.dashboard.service;

import com.avh.practicas.asignacion.entity.EstadoAsignacion;
import com.avh.practicas.asignacion.repository.AsignacionRepository;
import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.auth.repository.AuthUsuarioRepository;
import com.avh.practicas.dashboard.dto.AlertaDto;
import com.avh.practicas.dashboard.dto.DashboardDto;
import com.avh.practicas.dashboard.dto.FiltrosResponse;
import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.empresa.repository.EmpresaRepository;
import com.avh.practicas.empresa.repository.TutorEmpresarialRepository;
import com.avh.practicas.estudiante.entity.DocenteAsesor;
import com.avh.practicas.estudiante.entity.EstadoPractica;
import com.avh.practicas.estudiante.repository.DocenteAsesorRepository;
import com.avh.practicas.estudiante.repository.EstudianteRepository;
import com.avh.practicas.estudiante.repository.InstanciaPracticaRepository;
import com.avh.practicas.reporte.dto.ReporteResumenDto;
import com.avh.practicas.reporte.service.ReporteService;
import com.avh.practicas.shared.enums.Rol;
import com.avh.practicas.vinculacion.repository.ConvenioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ReporteService reporteService;
    private final AsignacionRepository asignacionRepository;
    private final EstudianteRepository estudianteRepository;
    private final EmpresaRepository empresaRepository;
    private final AuthUsuarioRepository usuarioRepository;
    private final InstanciaPracticaRepository instanciaPracticaRepository;
    private final DocenteAsesorRepository docenteAsesorRepository;
    private final TutorEmpresarialRepository tutorEmpresarialRepository;
    private final ConvenioRepository convenioRepository;

    @Transactional(readOnly = true)
    public DashboardDto getDashboard() {
        Usuario usuario = obtenerUsuarioAutenticado();
        if (usuario != null && usuario.getRol() == Rol.TUTOR_EMPRESARIAL) {
            return construirDashboardTutor(usuario);
        }

        ReporteResumenDto reporte = reporteService.resumen();

        long estudiantesEnPractica = instanciaPracticaRepository.countByEstado(EstadoPractica.EN_CURSO);
        long asignacionesActivas = asignacionRepository.countByEstadoNot(EstadoAsignacion.CANCELADA);
        long empresasActivas = empresaRepository.countByActivoTrue();
        long usuariosActivos = usuarioRepository.countByActivoTrue();
        long cierresPendientes = instanciaPracticaRepository.countPracticasEnCursoSinNotaFinal();
        long estudiantesSinEvaluar = cierresPendientes;
        long aptosSinIniciar = estudianteRepository.countAptosSinPracticaActiva();

        long estudiantesAsignados = 0L;
        long calificacionesPendientes = 0L;
        String correo = obtenerCorreoAutenticado();
        if (correo != null) {
            DocenteAsesor docente = docenteAsesorRepository.findByCorreo(correo).orElse(null);
            if (docente != null) {
                estudiantesAsignados = instanciaPracticaRepository
                        .countByDocenteAsesorIdAndEstado(docente.getId(), EstadoPractica.EN_CURSO);
                calificacionesPendientes = instanciaPracticaRepository
                        .countPracticasEnCursoSinNotaFinalPorDocente(docente.getId());
            }
        }

        return DashboardDto.builder()
                .totalEstudiantes(reporte.estudiantesRegistrados())
                .totalEmpresas(empresasActivas)
                .totalVacantes(reporte.totalVacantes())
                .usuariosActivos(usuariosActivos)
                .estudiantesEnPractica(estudiantesEnPractica)
                .empresasActivas(empresasActivas)
                .vacantesActivas(reporte.vacantesActivas())
                .vacantesParaAprobar(reporte.vacantesPendientes())
                .asignacionesActivas(asignacionesActivas)
                .cierresPendientes(cierresPendientes)
                .estudiantesSinEvaluar(estudiantesSinEvaluar)
                .aptosSinIniciar(aptosSinIniciar)
                .estudiantesAsignados(estudiantesAsignados)
                .calificacionesPendientes(calificacionesPendientes)
                .build();
    }

    private DashboardDto construirDashboardTutor(Usuario usuario) {
        TutorEmpresarial tutor = tutorEmpresarialRepository.findByCorreo(usuario.getCorreo()).orElse(null);
        if (tutor == null) {
            return DashboardDto.builder()
                    .estudiantesAsignados(0L)
                    .firmasPendientes(0L)
                    .build();
        }

        long practicantes = instanciaPracticaRepository.countByTutorIdAndEstado(
                tutor.getId(), EstadoPractica.EN_CURSO);
        long firmasPendientes = convenioRepository.countPendienteFirmaTutor(tutor.getId());

        return DashboardDto.builder()
                .estudiantesAsignados(practicantes)
                .firmasPendientes(firmasPendientes)
                .build();
    }

    public List<AlertaDto> getAlertas() {
        return List.of(
                AlertaDto.builder().id(1L).mensaje("Nueva vacante creada").leida(false).build(),
                AlertaDto.builder().id(2L).mensaje("Estudiante asignado").leida(true).build()
        );
    }

    public void marcarAlertaLeida(Long id) {
        // Las alertas del panel usan /notificaciones; este endpoint se mantiene por compatibilidad.
    }

    public FiltrosResponse getFiltrosDisponibles() {
        return FiltrosResponse.builder()
                .modalidades(List.of("Presencial", "Remoto"))
                .areas(List.of("Desarrollo", "Calidad", "Administración"))
                .build();
    }

    private String obtenerCorreoAutenticado() {
        Usuario usuario = obtenerUsuarioAutenticado();
        return usuario != null ? usuario.getCorreo() : null;
    }

    private Usuario obtenerUsuarioAutenticado() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof String correo)) {
            return null;
        }
        return usuarioRepository.findByCorreo(correo).orElse(null);
    }
}
