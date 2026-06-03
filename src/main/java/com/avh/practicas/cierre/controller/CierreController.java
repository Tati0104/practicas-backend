package com.avh.practicas.cierre.controller;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.auth.repository.AuthUsuarioRepository;
import com.avh.practicas.cierre.checklist.dto.ResumenChecklist;
import com.avh.practicas.cierre.dto.CierrePracticaResponse;
import com.avh.practicas.cierre.dto.EjecutarCierreRequest;
import com.avh.practicas.cierre.entity.TipoEncuesta;
import com.avh.practicas.cierre.facade.FachadaCierrePractica;
import com.avh.practicas.shared.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * API de cierre de práctica (PE-43 — Facade).
 */
@RestController
@RequestMapping("/cierres")
@RequiredArgsConstructor
public class CierreController {

    private final FachadaCierrePractica fachadaCierrePractica;
    private final AuthUsuarioRepository usuarioRepository;

    @GetMapping("/{practicaId}/checklist")
    public ResponseEntity<ApiResponse<ResumenChecklist>> obtenerChecklist(@PathVariable Long practicaId) {
        ResumenChecklist resumen = fachadaCierrePractica.verificarChecklist(practicaId);
        return ResponseEntity.ok(ApiResponse.ok(resumen));
    }

    @PostMapping("/{practicaId}/ejecutar")
    @PreAuthorize("hasRole('COORD_PRACTICA')")
    public ResponseEntity<ApiResponse<CierrePracticaResponse>> ejecutarCierre(
            @PathVariable Long practicaId,
            @Valid @RequestBody EjecutarCierreRequest request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByCorreo(email)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró usuario asociado al correo: " + email));

        CierrePracticaResponse resultado = fachadaCierrePractica.ejecutarCierre(practicaId, usuario.getId());
        return ResponseEntity.ok(ApiResponse.ok("Cierre de práctica ejecutado", resultado));
    }

    @PostMapping("/{practicaId}/recordatorio/{tipo}")
    @PreAuthorize("hasAnyRole('COORD_PRACTICA', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> enviarRecordatorio(
            @PathVariable Long practicaId,
            @PathVariable TipoEncuesta tipo) {

        fachadaCierrePractica.enviarRecordatorioEncuesta(practicaId, tipo);
        return ResponseEntity.ok(ApiResponse.ok("Recordatorio enviado"));
    }
}
