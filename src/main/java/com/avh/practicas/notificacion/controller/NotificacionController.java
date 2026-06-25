package com.avh.practicas.notificacion.controller;

import com.avh.practicas.notificacion.service.NotificacionService;
import com.avh.practicas.seguimiento.entity.AlertaSistema;
import com.avh.practicas.shared.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @GetMapping
    public List<AlertaSistema> listar(@RequestParam(required = false) Long practicaId) {
        return notificacionService.listarParaUsuarioActual(practicaId);
    }

    @PatchMapping("/{id}/leer")
    public ResponseEntity<ApiResponse<Void>> marcarLeida(@PathVariable Long id) {
        notificacionService.marcarLeida(id);
        return ResponseEntity.ok(ApiResponse.ok("Notificación marcada como leída", null));
    }
}
