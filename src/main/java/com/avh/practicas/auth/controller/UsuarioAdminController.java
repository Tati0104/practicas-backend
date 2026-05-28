package com.avh.practicas.auth.controller;

import com.avh.practicas.auth.dto.UsuarioAdminRequest;
import com.avh.practicas.auth.dto.UsuarioAdminResponse;
import com.avh.practicas.auth.service.UsuarioAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/usuarios")
@RequiredArgsConstructor
public class UsuarioAdminController {

    private final UsuarioAdminService usuarioAdminService;

    @GetMapping
    public ResponseEntity<List<UsuarioAdminResponse>> listarUsuarios() {
        return ResponseEntity.ok(usuarioAdminService.listarUsuarios());
    }

    @PostMapping
    public ResponseEntity<UsuarioAdminResponse> crearUsuario(@Valid @RequestBody UsuarioAdminRequest request) {
        return ResponseEntity.ok(usuarioAdminService.crearUsuario(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioAdminResponse> actualizarUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioAdminRequest request) {
        return ResponseEntity.ok(usuarioAdminService.actualizarUsuario(id, request));
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<UsuarioAdminResponse> activarUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioAdminService.activarUsuario(id));
    }

    @PatchMapping("/{id}/inactivar")
    public ResponseEntity<UsuarioAdminResponse> inactivarUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioAdminService.inactivarUsuario(id));
    }
}