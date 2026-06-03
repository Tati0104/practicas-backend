package com.avh.practicas.seguimiento.controller;

import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.empresa.repository.TutorEmpresarialRepository;
import com.avh.practicas.estudiante.entity.DocenteAsesor;
import com.avh.practicas.estudiante.entity.Estudiante;
import com.avh.practicas.estudiante.repository.DocenteAsesorRepository;
import com.avh.practicas.estudiante.repository.EstudianteRepository;
import com.avh.practicas.seguimiento.dto.AvanceRequest;
import com.avh.practicas.seguimiento.dto.BitacoraRequest;
import com.avh.practicas.seguimiento.dto.ObservacionRequest;
import com.avh.practicas.seguimiento.dto.TableroResponse;
import com.avh.practicas.seguimiento.entity.AlertaSistema;
import com.avh.practicas.seguimiento.entity.AvanceTutor;
import com.avh.practicas.seguimiento.entity.BitacoraEstudiante;
import com.avh.practicas.seguimiento.entity.ObservacionDocente;
import com.avh.practicas.seguimiento.service.SeguimientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para exponer los servicios de seguimiento y monitoreo de prácticas.
 */
@RestController
@RequestMapping("/seguimiento")
@RequiredArgsConstructor
public class SeguimientoController {

    private final SeguimientoService service;
    private final DocenteAsesorRepository docenteRepository;
    private final TutorEmpresarialRepository tutorRepository;
    private final EstudianteRepository estudianteRepository;

    /**
     * Obtiene el tablero de seguimiento filtrado por programa, empresa, docente, corte y estado.
     * Solo permitido para coordinadores y secretarias.
     */
    @GetMapping("/tablero")
    @PreAuthorize("hasAnyRole('COORD_PRACTICA', 'SECRETARIA', 'ADMIN')")
    public List<TableroResponse> obtenerTablero(
            @RequestParam Long programaId,
            @RequestParam(required = false) String empresa,
            @RequestParam(required = false) String docente,
            @RequestParam(required = false) Integer corte,
            @RequestParam(required = false) String estadoSeguimiento) {
        return service.obtenerTableroSeguimiento(programaId, empresa, docente, corte, estadoSeguimiento);
    }

    /**
     * Registra una nueva observación académica para un corte de práctica.
     * Solo permitido para el Docente Asesor asignado.
     */
    @PostMapping("/{practicaId}/observaciones")
    @PreAuthorize("hasAnyRole('DOCENTE_ASESOR', 'ADMIN')")
    public ObservacionDocente registrarObservacion(
            @PathVariable Long practicaId,
            @RequestParam Integer corte,
            @Valid @RequestBody ObservacionRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        DocenteAsesor docente = docenteRepository.findByCorreo(email)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró docente asesor asociado al correo: " + email));
        return service.registrarObservacion(practicaId, docente.getId(), corte, request);
    }

    /**
     * Edita una observación docente por su ID.
     */
    @PutMapping("/observaciones/{id}")
    @PreAuthorize("hasAnyRole('DOCENTE_ASESOR', 'ADMIN')")
    public ObservacionDocente editarObservacion(
            @PathVariable Long id,
            @Valid @RequestBody ObservacionRequest request) {
        return service.editarObservacion(id, request);
    }

    /**
     * Registra un avance de tutor para un corte de práctica.
     * Solo permitido para el Tutor Empresarial asignado.
     */
    @PostMapping("/{practicaId}/avances-tutor")
    @PreAuthorize("hasAnyRole('TUTOR_EMPRESARIAL', 'ADMIN')")
    public AvanceTutor registrarAvanceTutor(
            @PathVariable Long practicaId,
            @RequestParam Integer corte,
            @Valid @RequestBody AvanceRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        TutorEmpresarial tutor = tutorRepository.findByCorreo(email)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró tutor asociado al correo: " + email));
        return service.registrarAvanceTutor(practicaId, tutor.getId(), corte, request);
    }

    /**
     * Registra una bitácora de actividades para un corte de práctica.
     * Solo permitido para el Estudiante propietario de la práctica.
     */
    @PostMapping("/{practicaId}/bitacora")
    @PreAuthorize("hasAnyRole('ESTUDIANTE', 'ADMIN')")
    public BitacoraEstudiante registrarBitacora(
            @PathVariable Long practicaId,
            @RequestParam Integer corte,
            @Valid @RequestBody BitacoraRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Estudiante estudiante = estudianteRepository.findByCorreo(email)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró estudiante asociado al correo: " + email));
        return service.registrarBitacoraEstudiante(practicaId, estudiante.getId(), corte, request);
    }

    /**
     * Obtiene todas las observaciones registradas para una práctica.
     */
    @GetMapping("/{practicaId}/observaciones")
    public List<ObservacionDocente> obtenerObservaciones(@PathVariable Long practicaId) {
        return service.obtenerObservacionesPorPractica(practicaId);
    }

    /**
     * Obtiene todos los avances de tutor registrados para una práctica.
     */
    @GetMapping("/{practicaId}/avances-tutor")
    public List<AvanceTutor> obtenerAvancesTutor(@PathVariable Long practicaId) {
        return service.obtenerAvancesPorPractica(practicaId);
    }

    /**
     * Obtiene la bitácora registrada para una práctica.
     */
    @GetMapping("/{practicaId}/bitacora")
    public List<BitacoraEstudiante> obtenerBitacora(@PathVariable Long practicaId) {
        return service.obtenerBitacorasPorPractica(practicaId);
    }

    /**
     * Obtiene las alertas de sistema activas.
     */
    @GetMapping("/alertas")
    @PreAuthorize("hasAnyRole('COORD_PRACTICA', 'DOCENTE_ASESOR', 'ADMIN')")
    public List<AlertaSistema> obtenerAlertas() {
        return service.obtenerAlertasActivas();
    }
}
