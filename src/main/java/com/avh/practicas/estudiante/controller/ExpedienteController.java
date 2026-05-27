package com.avh.practicas.estudiante.controller;

import com.avh.practicas.estudiante.entity.Expediente;
import com.avh.practicas.estudiante.repository.ExpedienteRepository;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import com.avh.practicas.shared.pattern.proxy.ScopeGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/expedientes")
@RequiredArgsConstructor
public class ExpedienteController {

    private final ExpedienteRepository expedienteRepository;

    @GetMapping("/{estudianteId}")
    @ScopeGuard("EXPEDIENTE_VER")
    public ResponseEntity<Expediente> obtenerPorEstudianteId(@PathVariable Long estudianteId) {
        Expediente expediente = expedienteRepository.findByEstudianteId(estudianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el expediente para el estudiante con id: " + estudianteId));
        return ResponseEntity.ok(expediente);
    }
}
