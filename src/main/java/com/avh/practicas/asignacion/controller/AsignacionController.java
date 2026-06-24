package com.avh.practicas.asignacion.controller;

import com.avh.practicas.asignacion.dto.AsignacionDetalleResponse;
import com.avh.practicas.asignacion.dto.AsignacionRequest;
import com.avh.practicas.asignacion.dto.AsignacionResponse;
import com.avh.practicas.asignacion.dto.CambiarEstadoAsignacionRequest;
import com.avh.practicas.asignacion.dto.CancelarAsignacionRequest;
import com.avh.practicas.asignacion.dto.EstudianteAptoResponse;
import com.avh.practicas.asignacion.entity.EstadoAsignacion;
import com.avh.practicas.asignacion.service.AsignacionService;
import com.avh.practicas.vacante.dto.VacanteResponse;
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

import java.util.List;

/**
 * Controller del modulo de asignaciones/postulaciones.
 * En el negocio, la postulacion oficial la realiza Coordinacion de Practicas,
 * por eso el endpoint principal delega en AsignacionService.asignar(...).
 */
@RestController("asignacionModuloController")
@RequestMapping("/asignaciones")
@RequiredArgsConstructor
public class AsignacionController {

    private final AsignacionService service;

    // Endpoint principal S4: crea la asignacion/postulacion por Coordinacion de Practicas.
    @PostMapping
    public AsignacionResponse asignar(@Valid @RequestBody AsignacionRequest request) {
        return service.asignar(request);
    }

    @GetMapping
    public Page<AsignacionResponse> listar(
            @RequestParam(required = false) Long estudianteId,
            @RequestParam(required = false) Long vacanteId,
            @RequestParam(required = false) Long coordinadorId,
            @RequestParam(required = false) EstadoAsignacion estado,
            Pageable pageable) {
        return service.listar(estudianteId, vacanteId, coordinadorId, estado, pageable);
    }

    // Endpoint de trazabilidad PE-31: retorna detalle e historial de cambios de estado.
    @GetMapping("/{id}")
    public AsignacionDetalleResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PatchMapping("/{id}/iniciar-vinculacion")
    public AsignacionResponse iniciarVinculacion(@PathVariable Long id,
                                                 @Valid @RequestBody CambiarEstadoAsignacionRequest request) {
        return service.iniciarVinculacion(id, request.responsableId(), request.motivo());
    }

    @PatchMapping("/{id}/completar-vinculacion")
    public AsignacionResponse completarVinculacion(@PathVariable Long id,
                                                   @Valid @RequestBody CambiarEstadoAsignacionRequest request) {
        return service.completarVinculacion(id, request.responsableId(), request.motivo());
    }

    // Cambio de estado usando patron State: libera cupo si la asignacion se cancela.
    @PatchMapping("/{id}/cancelar")
    public AsignacionResponse cancelar(@PathVariable Long id,
                                       @Valid @RequestBody CancelarAsignacionRequest request) {
        return service.cancelar(id, request.responsableId(), request.motivo());
    }

    // Apoyo PE-31: lista estudiantes APTO para que Coordinacion pueda asignarlos.
    @GetMapping("/estudiantes-aptos")
    public List<EstudianteAptoResponse> estudiantesAptos(
            @RequestParam(required = false) Long programaId,
            @RequestParam(required = false) Long vacanteId) {
        return service.listarEstudiantesAptos(programaId, vacanteId);
    }

    // Apoyo PE-31: lista vacantes activas con cupos disponibles.
    @GetMapping("/vacantes-activas")
    public List<VacanteResponse> vacantesActivas(@RequestParam(required = false) Long programaId) {
        return service.listarVacantesActivas(programaId);
    }
}
