package com.avh.practicas.usuario.controller;

import com.avh.practicas.shared.dto.ApiResponse;
import com.avh.practicas.usuario.dto.*;
import com.avh.practicas.usuario.service.UsuarioAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// SOLID · SRP — única responsabilidad: recibir peticiones HTTP y delegar al service
@RestController
@RequestMapping("/admin/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioAdminController {

    private final UsuarioAdminService usuarioAdminService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UsuarioDto>>> listar(
            FiltroUsuarioRequest filtros,
            Pageable pageable) {
        return ResponseEntity.ok(
            ApiResponse.ok(usuarioAdminService.listar(filtros, pageable)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UsuarioDto>> crear(
            @Valid @RequestBody CrearUsuarioRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(
                usuarioAdminService.crear(dto), "Usuario creado correctamente"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioDto>> editar(
            @PathVariable Long id,
            @Valid @RequestBody EditarUsuarioRequest dto) {
        return ResponseEntity.ok(
            ApiResponse.ok(usuarioAdminService.editar(id, dto)));
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<ApiResponse<Void>> activar(@PathVariable Long id) {
        usuarioAdminService.activar(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Usuario activado"));
    }

    @PatchMapping("/{id}/inactivar")
    public ResponseEntity<ApiResponse<Void>> inactivar(@PathVariable Long id) {
        usuarioAdminService.inactivar(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Usuario inactivado"));
    }
}