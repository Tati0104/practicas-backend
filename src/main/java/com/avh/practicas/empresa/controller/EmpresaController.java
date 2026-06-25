package com.avh.practicas.empresa.controller;

import com.avh.practicas.empresa.dto.InactivarEmpresaRequest;
import com.avh.practicas.empresa.entity.Empresa;
import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.empresa.service.EmpresaService;
import com.avh.practicas.empresa.service.TutorEmpresarialService;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import com.avh.practicas.shared.pattern.proxy.ScopeGuard;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;
    private final TutorEmpresarialService tutorEmpresarialService;

    @GetMapping
    @ScopeGuard("EMPRESA_LISTAR")
    public ResponseEntity<Page<Empresa>> listar(
            @RequestParam(required = false) String sector,
            @RequestParam(required = false) String programa,
            @RequestParam(required = false) Boolean activo,
            Pageable pageable) {
        Page<Empresa> empresas = empresaService.listar(sector, programa, activo, pageable);
        return ResponseEntity.ok(empresas);
    }

    @GetMapping("/{id}")
    @ScopeGuard("EMPRESA_LISTAR")
    public ResponseEntity<Empresa> obtenerPorId(@PathVariable Long id) {
        Empresa empresa = empresaService.obtenerPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la empresa con id: " + id));
        return ResponseEntity.ok(empresa);
    }

    @PostMapping
    @ScopeGuard("EMPRESA_REGISTRAR")
    public ResponseEntity<Empresa> registrar(@Valid @RequestBody Empresa empresa) {
        Empresa nuevaEmpresa = empresaService.registrar(empresa);
        return new ResponseEntity<>(nuevaEmpresa, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @ScopeGuard("EMPRESA_EDITAR")
    public ResponseEntity<Empresa> editar(@PathVariable Long id, @Valid @RequestBody Empresa empresa) {
        Empresa empresaEditada = empresaService.editar(id, empresa);
        return ResponseEntity.ok(empresaEditada);
    }

    @PatchMapping("/{id}")
    @ScopeGuard("EMPRESA_EDITAR")
    public ResponseEntity<Empresa> actualizacionParcial(@PathVariable Long id, @RequestBody Map<String, Object> campos) {
        Empresa empresaExistente = empresaService.obtenerPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la empresa con id: " + id));

        if (campos.containsKey("razonSocial")) {
            empresaExistente.setRazonSocial((String) campos.get("razonSocial"));
        }
        if (campos.containsKey("direccion")) {
            empresaExistente.setDireccion((String) campos.get("direccion"));
        }
        if (campos.containsKey("municipio")) {
            empresaExistente.setMunicipio((String) campos.get("municipio"));
        }
        if (campos.containsKey("telefono")) {
            empresaExistente.setTelefono((String) campos.get("telefono"));
        }
        if (campos.containsKey("activo")) {
            empresaExistente.setActivo((Boolean) campos.get("activo"));
        }

        Empresa empresaGuardada = empresaService.editar(id, empresaExistente);
        return ResponseEntity.ok(empresaGuardada);
    }

    @PatchMapping("/{id}/desactivar")
    @ScopeGuard("EMPRESA_DESACTIVAR")
    public ResponseEntity<Void> desactivar(
            @PathVariable Long id,
            @Valid @RequestBody InactivarEmpresaRequest request) {
        empresaService.desactivar(id, request.motivo());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activar")
    @ScopeGuard("EMPRESA_ACTIVAR")
    public ResponseEntity<Void> activar(@PathVariable Long id) {
        empresaService.activar(id);
        return ResponseEntity.noContent().build();
    }

    // Endpoint GET /empresas/{id}/tutores
    @GetMapping("/{id}/tutores")
    @ScopeGuard("TUTOR_LISTAR")
    public ResponseEntity<List<TutorEmpresarial>> listarTutoresPorEmpresa(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "false") boolean soloActivos) {
        
        // Validar existencia de la empresa
        empresaService.obtenerPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la empresa con id: " + id));

        if (soloActivos) {
            return ResponseEntity.ok(tutorEmpresarialService.obtenerActivosPorEmpresa(id));
        }
        return ResponseEntity.ok(tutorEmpresarialService.obtenerPorEmpresa(id));
    }
}
