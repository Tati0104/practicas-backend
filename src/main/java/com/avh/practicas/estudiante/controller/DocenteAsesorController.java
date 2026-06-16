package com.avh.practicas.estudiante.controller;

import com.avh.practicas.estudiante.dto.DocenteAsesorRequest;
import com.avh.practicas.estudiante.dto.DocenteAsesorResponse;
import com.avh.practicas.estudiante.service.DocenteAsesorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/docentes-asesores")
@RequiredArgsConstructor
public class DocenteAsesorController {

    private final DocenteAsesorService service;

    @GetMapping
    public List<DocenteAsesorResponse> listarPorPrograma(@RequestParam(required = false) Long programaId) {
        if (programaId == null) {
            return service.listarTodos();
        }
        return service.listarPorPrograma(programaId);
    }

    @GetMapping("/{id}")
    public DocenteAsesorResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public DocenteAsesorResponse registrar(@Valid @RequestBody DocenteAsesorRequest request) {
        return service.registrar(request);
    }

    @PutMapping("/{id}")
    public DocenteAsesorResponse editar(@PathVariable Long id, @Valid @RequestBody DocenteAsesorRequest request) {
        return service.editar(id, request);
    }

    @PatchMapping("/{id}/activar")
    public DocenteAsesorResponse activar(@PathVariable Long id) {
        return service.activar(id);
    }

    @PatchMapping("/{id}/inactivar")
    public DocenteAsesorResponse inactivar(@PathVariable Long id) {
        return service.inactivar(id);
    }
}
