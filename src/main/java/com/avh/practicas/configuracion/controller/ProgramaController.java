package com.avh.practicas.configuracion.controller;

import com.avh.practicas.configuracion.entity.Programa;
import com.avh.practicas.configuracion.service.ProgramaService;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProgramaController {

    private final ProgramaService programaService;

    @GetMapping("/programas")
    public ResponseEntity<List<Programa>> obtenerTodos() {
        return ResponseEntity.ok(programaService.obtenerTodos());
    }

    @GetMapping("/programas/{id}")
    public ResponseEntity<Programa> obtenerPorId(@PathVariable Long id) {
        Programa programa = programaService.obtenerPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el programa con id: " + id));
        return ResponseEntity.ok(programa);
    }

    @PostMapping("/programas")
    public ResponseEntity<Programa> crear(@Valid @RequestBody Programa programa) {
        Programa nuevoPrograma = programaService.crear(programa);
        return new ResponseEntity<>(nuevoPrograma, HttpStatus.CREATED);
    }

    @PutMapping("/programas/{id}")
    public ResponseEntity<Programa> editar(@PathVariable Long id, @Valid @RequestBody Programa programa) {
        Programa programaEditado = programaService.editar(id, programa);
        return ResponseEntity.ok(programaEditado);
    }

    @PatchMapping("/programas/{id}")
    public ResponseEntity<Programa> actualizacionParcial(@PathVariable Long id, @RequestBody Map<String, Object> campos) {
        Programa programaExistente = programaService.obtenerPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el programa con id: " + id));

        if (campos.containsKey("nombre")) {
            programaExistente.setNombre((String) campos.get("nombre"));
        }
        if (campos.containsKey("totalPracticas")) {
            programaExistente.setTotalPracticas((Integer) campos.get("totalPracticas"));
        }
        if (campos.containsKey("activo")) {
            programaExistente.setActivo((Boolean) campos.get("activo"));
        }

        Programa programaGuardado = programaService.editar(id, programaExistente);
        return ResponseEntity.ok(programaGuardado);
    }

    @PatchMapping("/programas/{id}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        programaService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/programas/{id}/activar")
    public ResponseEntity<Void> activar(@PathVariable Long id) {
        programaService.activar(id);
        return ResponseEntity.noContent().build();
    }

    // Endpoint GET /facultades/{id}/programas
    @GetMapping("/facultades/{facultadId}/programas")
    public ResponseEntity<List<Programa>> obtenerPorFacultad(
            @PathVariable Long facultadId,
            @RequestParam(required = false, defaultValue = "false") boolean soloActivos) {
        if (soloActivos) {
            return ResponseEntity.ok(programaService.obtenerActivosPorFacultad(facultadId));
        }
        return ResponseEntity.ok(programaService.obtenerPorFacultad(facultadId));
    }
}
