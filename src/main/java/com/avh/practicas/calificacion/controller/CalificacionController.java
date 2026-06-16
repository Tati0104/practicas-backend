package com.avh.practicas.calificacion.controller;

import com.avh.practicas.calificacion.dto.NotaFinalRequest;
import com.avh.practicas.calificacion.dto.NotaRequest;
import com.avh.practicas.calificacion.dto.ResumenCalificacionesResponse;
import com.avh.practicas.calificacion.entity.NotaDocente;
import com.avh.practicas.calificacion.entity.NotaFinal;
import com.avh.practicas.calificacion.entity.NotaTutor;
import com.avh.practicas.calificacion.service.CalificacionService;
import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.empresa.repository.TutorEmpresarialRepository;
import com.avh.practicas.estudiante.entity.DocenteAsesor;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.estudiante.repository.DocenteAsesorRepository;
import com.avh.practicas.estudiante.repository.InstanciaPracticaRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para el registro y consulta de calificaciones de prácticas.
 */
@RestController
@RequestMapping("/calificaciones")
@RequiredArgsConstructor
public class CalificacionController {

    private final CalificacionService service;
    private final DocenteAsesorRepository docenteRepository;
    private final TutorEmpresarialRepository tutorRepository;
    private final InstanciaPracticaRepository practicaRepository;

    @PostMapping("/{practicaId}/docente")
    @PreAuthorize("hasAnyRole('DOCENTE_ASESOR', 'ADMIN', 'COORD_PRACTICA')")
    public NotaDocente registrarNotaDocente(
            @PathVariable Long practicaId,
            @RequestParam Integer corte,
            @Valid @RequestBody NotaRequest request) {
        Long docenteId = resolverDocenteId(practicaId);
        return service.registrarNotaDocente(practicaId, docenteId, corte, request);
    }

    @PostMapping("/{practicaId}/tutor")
    @PreAuthorize("hasAnyRole('TUTOR_EMPRESARIAL', 'ADMIN', 'COORD_PRACTICA')")
    public NotaTutor registrarNotaTutor(
            @PathVariable Long practicaId,
            @RequestParam Integer corte,
            @Valid @RequestBody NotaRequest request) {
        Long tutorId = resolverTutorId(practicaId);
        return service.registrarNotaTutor(practicaId, tutorId, corte, request);
    }

    @PostMapping("/{practicaId}/final")
    @PreAuthorize("hasAnyRole('DOCENTE_ASESOR', 'ADMIN', 'COORD_PRACTICA')")
    public NotaFinal registrarNotaFinal(
            @PathVariable Long practicaId,
            @Valid @RequestBody NotaFinalRequest request) {
        Long docenteId = resolverDocenteId(practicaId);
        return service.registrarNotaFinal(practicaId, docenteId, request);
    }

    @GetMapping("/{practicaId}/resumen")
    public ResumenCalificacionesResponse obtenerResumen(@PathVariable Long practicaId) {
        return service.obtenerResumen(practicaId);
    }

    private Long resolverDocenteId(Long practicaId) {
        if (tieneRol("DOCENTE_ASESOR") && !puedeActuarComoCoordinador()) {
            String email = obtenerCorreoAutenticado();
            DocenteAsesor docente = docenteRepository.findByCorreo(email)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No se encontró docente asesor asociado al correo: " + email));
            return docente.getId();
        }

        InstanciaPractica practica = practicaRepository.findById(practicaId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la práctica con ID: " + practicaId));
        if (practica.getDocenteAsesorId() == null) {
            throw new IllegalArgumentException("La práctica no tiene docente asesor asignado.");
        }
        return practica.getDocenteAsesorId();
    }

    private Long resolverTutorId(Long practicaId) {
        if (tieneRol("TUTOR_EMPRESARIAL") && !puedeActuarComoCoordinador()) {
            String email = obtenerCorreoAutenticado();
            TutorEmpresarial tutor = tutorRepository.findByCorreo(email)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No se encontró tutor asociado al correo: " + email));
            return tutor.getId();
        }

        InstanciaPractica practica = practicaRepository.findById(practicaId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la práctica con ID: " + practicaId));
        if (practica.getTutorId() == null) {
            throw new IllegalArgumentException("La práctica no tiene tutor empresarial asignado.");
        }
        return practica.getTutorId();
    }

    private boolean puedeActuarComoCoordinador() {
        return tieneRol("ADMIN") || tieneRol("COORD_PRACTICA");
    }

    private boolean tieneRol(String rol) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        for (GrantedAuthority authority : auth.getAuthorities()) {
            String name = authority.getAuthority();
            if (rol.equals(name) || ("ROLE_" + rol).equals(name)) {
                return true;
            }
        }
        return false;
    }

    private String obtenerCorreoAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "";
    }
}
