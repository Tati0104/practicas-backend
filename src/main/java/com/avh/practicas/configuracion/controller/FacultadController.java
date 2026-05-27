package com.avh.practicas.configuracion.controller;

import com.avh.practicas.configuracion.entity.Facultad;
import com.avh.practicas.configuracion.service.FacultadService;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/facultades")
@RequiredArgsConstructor
public class FacultadController {

    private final FacultadService facultadService;

    @GetMapping
    public ResponseEntity<List<Facultad>> obtenerTodas() {
        return ResponseEntity.ok(facultadService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Facultad> obtenerPorId(@PathVariable Long id) {
        Facultad facultad = facultadService.obtenerPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la facultad con id: " + id));
        return ResponseEntity.ok(facultad);
    }

    @PostMapping
    public ResponseEntity<Facultad> crear(@Valid @RequestBody Facultad facultad) {
        Facultad nuevaFacultad = facultadService.crear(facultad);
        return new ResponseEntity<>(nuevaFacultad, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Facultad> editar(@PathVariable Long id, @Valid @RequestBody Facultad facultad) {
        Facultad facultadEditada = facultadService.editar(id, facultad);
        return ResponseEntity.ok(facultadEditada);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Facultad> actualizacionParcial(@PathVariable Long id, @RequestBody Map<String, Object> campos) {
        Facultad facultadExistente = facultadService.obtenerPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la facultad con id: " + id));

        if (campos.containsKey("nombre")) {
            facultadExistente.setNombre((String) campos.get("nombre"));
        }
        if (campos.containsKey("activo")) {
            Boolean activo = (Boolean) campos.get("activo");
            facultadExistente.setActivo(activo);
        }

        Facultad facultadGuardada = facultadService.editar(id, facultadExistente);
        return ResponseEntity.ok(facultadGuardada);
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        facultadService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<Void> activar(@PathVariable Long id) {
        facultadService.activar(id);
        return ResponseEntity.noContent().build();
    }
}
