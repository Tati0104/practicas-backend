package com.avh.practicas.vacante.controller;

import com.avh.practicas.vacante.dto.RechazarVacanteRequest;
import com.avh.practicas.vacante.dto.VacanteRequest;
import com.avh.practicas.vacante.dto.VacanteResponse;
import com.avh.practicas.vacante.entity.EstadoVacanteEnum;
import com.avh.practicas.vacante.service.VacanteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vacantes")
@RequiredArgsConstructor
public class VacanteController {

    private final VacanteService service;

    @GetMapping
    public Page<VacanteResponse> listar(
            @RequestParam(required = false) Long empresaId,
            @RequestParam(required = false) Long programaId,
            @RequestParam(required = false) EstadoVacanteEnum estado,
            @RequestParam(required = false) String modalidad,
            @RequestParam(required = false) String area,
            Pageable pageable) {
        return service.listar(empresaId, programaId, estado, modalidad, area, pageable);
    }

    @GetMapping("/{id}")
    public VacanteResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public VacanteResponse crear(@Valid @RequestBody VacanteRequest request) {
        return service.crear(request);
    }

    @PatchMapping("/{id}/aprobar")
    public VacanteResponse aprobar(@PathVariable Long id,
                                   @RequestParam(required = false) Long aprobadoPorId) {
        return service.aprobar(id, aprobadoPorId);
    }

    @PatchMapping("/{id}/rechazar")
    public VacanteResponse rechazar(@PathVariable Long id, @Valid @RequestBody RechazarVacanteRequest request) {
        return service.rechazar(id, request.motivo());
    }

    @PatchMapping("/{id}/pausar")
    public VacanteResponse pausar(@PathVariable Long id) {
        return service.pausar(id);
    }

    @PatchMapping("/{id}/reactivar")
    public VacanteResponse reactivar(@PathVariable Long id) {
        return service.reactivar(id);
    }

    @PatchMapping("/{id}/cerrar")
    public VacanteResponse cerrar(@PathVariable Long id) {
        return service.cerrar(id);
    }

    @PatchMapping("/{id}/descontar-cupo")
    public VacanteResponse descontarCupo(@PathVariable Long id) {
        return service.descontarCupo(id);
    }

    @PatchMapping("/{id}/liberar-cupo")
    public VacanteResponse liberarCupo(@PathVariable Long id) {
        return service.liberarCupo(id);
    }
}
