package com.avh.practicas.configuracion.controller;

import com.avh.practicas.configuracion.entity.CatalogoPractica;
import com.avh.practicas.configuracion.service.CatalogoPracticaService;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/configuracion/catalogo")
@RequiredArgsConstructor
public class CatalogoPracticaController {

    private final CatalogoPracticaService catalogoPracticaService;

    @GetMapping
    public ResponseEntity<List<CatalogoPractica>> obtenerCatalogo(
            @RequestParam(required = false) Long programaId,
            @RequestParam(required = false, defaultValue = "false") boolean soloActivos) {
        if (programaId != null) {
            if (soloActivos) {
                return ResponseEntity.ok(catalogoPracticaService.obtenerActivosPorPrograma(programaId));
            }
            return ResponseEntity.ok(catalogoPracticaService.obtenerPorPrograma(programaId));
        }
        return ResponseEntity.ok(catalogoPracticaService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CatalogoPractica> obtenerPorId(@PathVariable Long id) {
        CatalogoPractica catalogoPractica = catalogoPracticaService.obtenerPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el catálogo de práctica con id: " + id));
        return ResponseEntity.ok(catalogoPractica);
    }

    @PostMapping
    public ResponseEntity<CatalogoPractica> crear(@Valid @RequestBody CatalogoPractica catalogoPractica) {
        CatalogoPractica nuevoCatalogo = catalogoPracticaService.crear(catalogoPractica);
        return new ResponseEntity<>(nuevoCatalogo, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CatalogoPractica> editar(@PathVariable Long id, @Valid @RequestBody CatalogoPractica catalogoPractica) {
        CatalogoPractica catalogoEditado = catalogoPracticaService.editar(id, catalogoPractica);
        return ResponseEntity.ok(catalogoEditado);
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        catalogoPracticaService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<Void> activar(@PathVariable Long id) {
        catalogoPracticaService.activar(id);
        return ResponseEntity.noContent().build();
    }
}
