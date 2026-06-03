package com.avh.practicas.vinculacion.alerta.controller;

import com.avh.practicas.shared.api.ApiResponse;
import com.avh.practicas.vinculacion.alerta.dto.AlertaVista;
import com.avh.practicas.vinculacion.alerta.dto.CrearAlertaRequest;
import com.avh.practicas.vinculacion.alerta.service.AlertaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/vinculaciones/alertas")
@RequiredArgsConstructor
public class AlertaController {

    private final AlertaService alertaService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AlertaVista>>> listar(
            @RequestParam(required = false) Map<String, String> contexto
    ) {
        return ResponseEntity.ok(ApiResponse.ok(alertaService.listarActivas(contexto)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AlertaVista>> crear(@Valid @RequestBody CrearAlertaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Alerta creada", alertaService.crear(request)));
    }

    @PatchMapping("/{id}/resolver")
    public ResponseEntity<ApiResponse<AlertaVista>> resolver(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Alerta resuelta", alertaService.resolver(id)));
    }

    @PostMapping("/evaluar-auto-resolucion")
    public ResponseEntity<ApiResponse<Void>> evaluarAutoResolucion(
            @RequestBody(required = false) Map<String, String> contexto
    ) {
        alertaService.evaluarAutoResolucionGlobal(contexto);
        return ResponseEntity.ok(ApiResponse.ok("Auto-resolución evaluada"));
    }
}
