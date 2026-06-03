package com.avh.practicas.vinculacion.controller;

import com.avh.practicas.shared.api.ApiResponse;
import com.avh.practicas.vinculacion.mediator.MediadorVinculacion;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vinculaciones")
@RequiredArgsConstructor
public class VinculacionController {

    private final MediadorVinculacion mediadorVinculacion;

    @PostMapping("/practicas/{practicaId}/confirmar")
    public ResponseEntity<ApiResponse<Void>> confirmar(@PathVariable Long practicaId) {
        mediadorVinculacion.confirmarVinculacion(practicaId);
        return ResponseEntity.ok(ApiResponse.ok("Vinculación confirmada correctamente"));
    }
}
