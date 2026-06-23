package com.avh.practicas.dashboard.controller;

import com.avh.practicas.dashboard.dto.AlertaDto;
import com.avh.practicas.dashboard.dto.DashboardDto;
import com.avh.practicas.dashboard.dto.DashboardGerencialDto;
import com.avh.practicas.dashboard.dto.FiltrosResponse;
import com.avh.practicas.dashboard.dto.PanelEstudianteDto;
import com.avh.practicas.dashboard.service.DashboardGerencialService;
import com.avh.practicas.dashboard.service.DashboardService;
import com.avh.practicas.dashboard.service.EstudiantePanelService;
import com.avh.practicas.shared.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final DashboardGerencialService dashboardGerencialService;
    private final EstudiantePanelService estudiantePanelService;

    /**
     * Indicadores gerenciales para Dirección (PE-45).
     */
    @GetMapping("/gerencial")
    @PreAuthorize("hasAnyRole('DIRECCION', 'ADMIN')")
    public ResponseEntity<ApiResponse<DashboardGerencialDto>> obtenerDashboardGerencial(
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) Long facultadId) {
        DashboardGerencialDto indicadores = dashboardGerencialService.obtenerIndicadores(periodo, facultadId);
        return ResponseEntity.ok(ApiResponse.ok(indicadores));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardDto>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getDashboard()));
    }

    /** Panel de inicio exclusivo del estudiante (solo su información y prácticas). */
    @GetMapping("/panel-estudiante")
    @PreAuthorize("hasRole('ESTUDIANTE')")
    public ResponseEntity<ApiResponse<PanelEstudianteDto>> getPanelEstudiante() {
        return ResponseEntity.ok(ApiResponse.ok(estudiantePanelService.obtenerPanelEstudiante()));
    }

    @GetMapping("/alertas")
    public List<AlertaDto> getAlertas() {
        return dashboardService.getAlertas();
    }

    @PatchMapping("/alertas/{id}/leer")
    public void marcarAlertaLeida(@PathVariable Long id) {
        dashboardService.marcarAlertaLeida(id);
    }

    @GetMapping("/filtros-disponibles")
    public FiltrosResponse getFiltros() {
        return dashboardService.getFiltrosDisponibles();
    }
}