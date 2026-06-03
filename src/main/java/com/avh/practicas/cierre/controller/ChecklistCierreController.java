package com.avh.practicas.cierre.controller;

import com.avh.practicas.cierre.checklist.dto.ResumenChecklist;
import com.avh.practicas.cierre.entity.TipoEncuesta;
import com.avh.practicas.cierre.service.ChecklistCierreService;
import com.avh.practicas.shared.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * API del checklist de cierre (PE-nuevo — Composite).
 */
@RestController
@RequestMapping("/cierre/checklist")
@RequiredArgsConstructor
public class ChecklistCierreController {

    private final ChecklistCierreService checklistCierreService;

    @GetMapping("/{practicaId}")
    public ResponseEntity<ApiResponse<ResumenChecklist>> obtenerResumen(@PathVariable Long practicaId) {
        ResumenChecklist resumen = checklistCierreService.obtenerResumen(practicaId);
        return ResponseEntity.ok(ApiResponse.ok(resumen));
    }

    @GetMapping("/{practicaId}/habilitar-cierre")
    public ResponseEntity<ApiResponse<Boolean>> habilitarCierre(@PathVariable Long practicaId) {
        boolean habilitado = checklistCierreService.habilitarBotonCierre(practicaId);
        return ResponseEntity.ok(ApiResponse.ok(habilitado));
    }

    @PostMapping("/{practicaId}/encuestas/{tipo}/recordatorio")
    @PreAuthorize("hasAnyRole('COORD_PRACTICA', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> enviarRecordatorio(
            @PathVariable Long practicaId,
            @PathVariable TipoEncuesta tipo) {
        checklistCierreService.enviarRecordatorioEncuesta(practicaId, tipo);
        return ResponseEntity.ok(ApiResponse.ok("Recordatorio enviado"));
    }
}
