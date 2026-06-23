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
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
     * Endpoint desacoplado del tablero anterior: no exige programaId, resuelve el scope
     * del usuario autenticado (ESTUDIANTE ve solo la suya, coordinadores ven su facultad).
     */
    @GetMapping("/practicas")
    @PreAuthorize("hasAnyRole('COORD_PRACTICA', 'COORD_ACADEMICA', 'SECRETARIA', 'ADMIN', 'ESTUDIANTE', 'DOCENTE_ASESOR', 'EMPRESA', 'TUTOR_EMPRESARIAL')")
    public List<TableroResponse> obtenerPracticasVisibles(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) Long programaId,
            @RequestParam(required = false) String estadoSeguimiento,
            @RequestParam(required = false) String estadoPractica) {
        return service.obtenerPracticasSeguimiento(busqueda, programaId, estadoSeguimiento, estadoPractica);
    }

    /**
     * Obtiene el detalle de la práctica y su historial.
     */
    @GetMapping("/{practicaId}")
    public com.avh.practicas.seguimiento.dto.PracticaDetalleResponse obtenerDetalle(@PathVariable Long practicaId) {
        return service.obtenerDetallePractica(practicaId);
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
     * Acepta multipart/form-data: campo 'descripcion' (texto) y 'archivo' (opcional).
     */
    @PostMapping(value = "/{practicaId}/bitacora", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ESTUDIANTE', 'ADMIN')")
    public BitacoraEstudiante registrarBitacora(
            @PathVariable Long practicaId,
            @RequestParam Integer corte,
            @RequestParam String descripcion,
            @RequestParam(required = false) MultipartFile archivo) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Estudiante estudiante = estudianteRepository.findByCorreo(email)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró estudiante asociado al correo: " + email));
        BitacoraEstudiante bitacora = service.registrarBitacoraEstudiante(
                practicaId, estudiante.getId(), corte, new BitacoraRequest(descripcion));
        if (archivo != null && !archivo.isEmpty()) {
            bitacora = service.adjuntarArchivoBitacora(bitacora.getId(), archivo);
        }
        return bitacora;
    }

    /**
     * Descarga el archivo de soporte adjunto a una entrada de bitácora.
     */
    @GetMapping("/bitacora/{bitacoraId}/archivo")
    @PreAuthorize("hasAnyRole('ESTUDIANTE', 'DOCENTE_ASESOR', 'ADMIN')")
    public ResponseEntity<Resource> descargarArchivoBitacora(@PathVariable Long bitacoraId) {
        Resource resource = service.descargarArchivoBitacora(bitacoraId);
        String nombre = service.nombreArchivoBitacora(bitacoraId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombre + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
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
    public List<AlertaSistema> obtenerAlertas() {
        return service.obtenerAlertasActivas();
    }
}
