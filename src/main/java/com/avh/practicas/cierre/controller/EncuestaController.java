package com.avh.practicas.cierre.controller;

import com.avh.practicas.cierre.dto.RespuestasRequest;
import com.avh.practicas.cierre.entity.Encuesta;
import com.avh.practicas.cierre.entity.TipoEncuesta;
import com.avh.practicas.cierre.service.EncuestaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para gestionar encuestas de practica.
 */
@RestController
@RequestMapping("/encuestas")
@RequiredArgsConstructor
public class EncuestaController {

    private final EncuestaService service;

    /**
     * Obtiene la encuesta asociada a una practica y tipo si ya fue habilitada.
     */
    @GetMapping("/{practicaId}/{tipo}")
    public ResponseEntity<Encuesta> obtenerEncuesta(
            @PathVariable Long practicaId,
            @PathVariable TipoEncuesta tipo) {
        return service.obtenerPorPracticaYTipo(practicaId, tipo)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    /**
     * Guarda el borrador de respuestas para una encuesta especifica.
     */
    @PostMapping("/{id}/borrador")
    public Encuesta guardarBorrador(
            @PathVariable Long id,
            @RequestBody RespuestasRequest request) {
        return service.guardarBorrador(id, request.respuestasJson());
    }

    /**
     * Valida y finaliza la encuesta marcandola como COMPLETADA.
     */
    @PostMapping("/{id}/enviar")
    public Encuesta enviar(@PathVariable Long id) {
        return service.enviar(id);
    }

    /**
     * Envia manualmente un recordatorio de encuesta pendiente.
     */
    @PostMapping("/{practicaId}/{tipo}/recordatorio")
    @PreAuthorize("hasAnyRole('COORD_PRACTICA', 'ADMIN')")
    public void enviarRecordatorio(
            @PathVariable Long practicaId,
            @PathVariable TipoEncuesta tipo) {
        service.enviarRecordatorio(practicaId, tipo);
    }

    /**
     * Habilita la encuesta si no existe y envia la invitacion al destinatario.
     */
    @PostMapping("/{practicaId}/{tipo}/invitar")
    @PreAuthorize("hasAnyRole('COORD_PRACTICA', 'ADMIN')")
    public Encuesta enviarInvitacion(
            @PathVariable Long practicaId,
            @PathVariable TipoEncuesta tipo) {
        return service.enviarInvitacion(practicaId, tipo);
    }
}
