package com.avh.practicas.empresa.controller;

import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.empresa.service.TutorEmpresarialService;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import com.avh.practicas.shared.pattern.proxy.ScopeGuard;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tutores")
@RequiredArgsConstructor
public class TutorEmpresarialController {

    private final TutorEmpresarialService tutorEmpresarialService;

    @GetMapping("/{id}")
    @ScopeGuard("TUTOR_VER")
    public ResponseEntity<TutorEmpresarial> obtenerPorId(@PathVariable Long id) {
        TutorEmpresarial tutor = tutorEmpresarialService.obtenerPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el tutor con id: " + id));
        return ResponseEntity.ok(tutor);
    }

    @PostMapping
    @ScopeGuard("TUTOR_REGISTRAR")
    public ResponseEntity<TutorEmpresarial> registrar(@Valid @RequestBody TutorEmpresarial tutor) {
        TutorEmpresarial nuevoTutor = tutorEmpresarialService.registrar(tutor);
        return new ResponseEntity<>(nuevoTutor, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @ScopeGuard("TUTOR_EDITAR")
    public ResponseEntity<TutorEmpresarial> editar(@PathVariable Long id, @Valid @RequestBody TutorEmpresarial tutor) {
        TutorEmpresarial tutorEditado = tutorEmpresarialService.editar(id, tutor);
        return ResponseEntity.ok(tutorEditado);
    }

    @PatchMapping("/{id}/desactivar")
    @ScopeGuard("TUTOR_DESACTIVAR")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        tutorEmpresarialService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activar")
    @ScopeGuard("TUTOR_ACTIVAR")
    public ResponseEntity<Void> activar(@PathVariable Long id) {
        tutorEmpresarialService.activar(id);
        return ResponseEntity.noContent().build();
    }
}
