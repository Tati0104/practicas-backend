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
import com.avh.practicas.estudiante.repository.DocenteAsesorRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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

    /**
     * Registra o actualiza la nota de un corte dada por el Docente Asesor.
     */
    @PostMapping("/{practicaId}/docente")
    @PreAuthorize("hasAnyRole('DOCENTE_ASESOR', 'ADMIN')")
    public NotaDocente registrarNotaDocente(
            @PathVariable Long practicaId,
            @RequestParam Integer corte,
            @Valid @RequestBody NotaRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        DocenteAsesor docente = docenteRepository.findByCorreo(email)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró docente asesor asociado al correo: " + email));
        return service.registrarNotaDocente(practicaId, docente.getId(), corte, request);
    }

    /**
     * Registra o actualiza la nota de un corte dada por el Tutor Empresarial.
     */
    @PostMapping("/{practicaId}/tutor")
    @PreAuthorize("hasAnyRole('TUTOR_EMPRESARIAL', 'ADMIN')")
    public NotaTutor registrarNotaTutor(
            @PathVariable Long practicaId,
            @RequestParam Integer corte,
            @Valid @RequestBody NotaRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        TutorEmpresarial tutor = tutorRepository.findByCorreo(email)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró tutor asociado al correo: " + email));
        return service.registrarNotaTutor(practicaId, tutor.getId(), corte, request);
    }

    /**
     * Registra la nota final definitiva (Docente Asesor).
     */
    @PostMapping("/{practicaId}/final")
    @PreAuthorize("hasAnyRole('DOCENTE_ASESOR', 'ADMIN')")
    public NotaFinal registrarNotaFinal(
            @PathVariable Long practicaId,
            @Valid @RequestBody NotaFinalRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        DocenteAsesor docente = docenteRepository.findByCorreo(email)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró docente asesor asociado al correo: " + email));
        return service.registrarNotaFinal(practicaId, docente.getId(), request);
    }

    /**
     * Obtiene el resumen de todas las calificaciones de la práctica.
     */
    @GetMapping("/{practicaId}/resumen")
    public ResumenCalificacionesResponse obtenerResumen(@PathVariable Long practicaId) {
        return service.obtenerResumen(practicaId);
    }
}
