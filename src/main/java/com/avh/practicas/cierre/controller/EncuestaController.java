package com.avh.practicas.cierre.controller;

import com.avh.practicas.cierre.dto.RespuestasRequest;
import com.avh.practicas.cierre.entity.Encuesta;
import com.avh.practicas.cierre.entity.TipoEncuesta;
import com.avh.practicas.cierre.service.EncuestaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestionar encuestas de estudiantes y tutores.
 */
@RestController
@RequestMapping("/encuestas")
@RequiredArgsConstructor
public class EncuestaController {

    private final EncuestaService service;

    /**
     * Obtiene la encuesta asociada a una práctica y tipo.
     * Si no existe, se crea automáticamente como PENDIENTE y se envía la invitación.
     */
    @GetMapping("/{practicaId}/{tipo}")
    public Encuesta obtenerEncuesta(
            @PathVariable Long practicaId,
            @PathVariable TipoEncuesta tipo) {
        return service.obtenerPorPracticaYTipo(practicaId, tipo)
                .orElseGet(() -> service.crearEncuestaPendiente(practicaId, tipo));
    }

    /**
     * Guarda el borrador de respuestas para una encuesta específica.
     */
    @PostMapping("/{id}/borrador")
    public Encuesta guardarBorrador(
            @PathVariable Long id,
            @RequestBody RespuestasRequest request) {
        return service.guardarBorrador(id, request.respuestasJson());
    }

    /**
     * Valida y finaliza la encuesta (marcando como COMPLETADA).
     */
    @PostMapping("/{id}/enviar")
    public Encuesta enviar(@PathVariable Long id) {
        return service.enviar(id);
    }

    /**
     * Envía manualmente un recordatorio de encuesta pendiente (máximo uno diario).
     */
    @PostMapping("/{practicaId}/{tipo}/recordatorio")
    @PreAuthorize("hasAnyRole('COORD_PRACTICA', 'ADMIN')")
    public void enviarRecordatorio(
            @PathVariable Long practicaId,
            @PathVariable TipoEncuesta tipo) {
        service.enviarRecordatorio(practicaId, tipo);
    }

    /**
     * Envía la invitación inicial (o la reenvía) al correo del estudiante o tutor.
     */
    @PostMapping("/{practicaId}/{tipo}/invitar")
    @PreAuthorize("hasAnyRole('COORD_PRACTICA', 'ADMIN')")
    public Encuesta enviarInvitacion(
            @PathVariable Long practicaId,
            @PathVariable TipoEncuesta tipo) {
        return service.enviarInvitacion(practicaId, tipo);
    }
}
