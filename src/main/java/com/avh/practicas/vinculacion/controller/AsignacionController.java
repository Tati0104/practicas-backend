package com.avh.practicas.vinculacion.controller;

import com.avh.practicas.shared.api.ApiResponse;
import com.avh.practicas.vinculacion.dto.AsignacionResponse;
import com.avh.practicas.vinculacion.dto.CancelarAsignacionRequest;
import com.avh.practicas.vinculacion.dto.CrearAsignacionRequest;
import com.avh.practicas.vinculacion.entity.EstadoAsignacion;
import com.avh.practicas.vinculacion.service.AsignacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vinculaciones/asignaciones")
@RequiredArgsConstructor
public class AsignacionController {

    private final AsignacionService asignacionService;

    @PostMapping
    public ResponseEntity<ApiResponse<AsignacionResponse>> crear(@Valid @RequestBody CrearAsignacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Asignación creada", asignacionService.crear(request)));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<AsignacionResponse>> cambiarEstado(
            @PathVariable Long id,
            @RequestParam EstadoAsignacion estado
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Estado actualizado",
                asignacionService.cambiarEstado(id, estado)
        ));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<ApiResponse<AsignacionResponse>> cancelar(
            @PathVariable Long id,
            @Valid @RequestBody CancelarAsignacionRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Asignación cancelada",
                asignacionService.cancelar(id, request)
        ));
    }
}
