package com.avh.practicas.configuracion.controller;

import com.avh.practicas.configuracion.entity.CatalogoItem;
import com.avh.practicas.configuracion.entity.TipoCatalogo;
import com.avh.practicas.configuracion.service.CatalogoMaestroService;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/catalogos")
@RequiredArgsConstructor
public class CatalogoMaestroController {

    private final CatalogoMaestroService catalogoMaestroService;

    @GetMapping
    public ResponseEntity<List<CatalogoItem>> obtenerCatalogos(
            @RequestParam(required = false) TipoCatalogo tipo,
            @RequestParam(required = false, defaultValue = "false") boolean soloActivos) {
        if (tipo != null) {
            if (soloActivos) {
                return ResponseEntity.ok(catalogoMaestroService.obtenerActivosPorTipo(tipo));
            }
            return ResponseEntity.ok(catalogoMaestroService.obtenerPorTipo(tipo));
        }
        return ResponseEntity.ok(catalogoMaestroService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CatalogoItem> obtenerPorId(@PathVariable Long id) {
        CatalogoItem item = catalogoMaestroService.obtenerPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el ítem del catálogo con id: " + id));
        return ResponseEntity.ok(item);
    }

    @PostMapping
    public ResponseEntity<CatalogoItem> crear(@Valid @RequestBody CatalogoItem item) {
        CatalogoItem nuevoItem = catalogoMaestroService.crear(item);
        return new ResponseEntity<>(nuevoItem, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CatalogoItem> editar(@PathVariable Long id, @Valid @RequestBody CatalogoItem item) {
        CatalogoItem itemEditado = catalogoMaestroService.editar(id, item);
        return ResponseEntity.ok(itemEditado);
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        catalogoMaestroService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<Void> activar(@PathVariable Long id) {
        catalogoMaestroService.activar(id);
        return ResponseEntity.noContent().build();
    }
}
