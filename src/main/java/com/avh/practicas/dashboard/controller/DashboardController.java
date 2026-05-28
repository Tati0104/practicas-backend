package com.avh.practicas.dashboard.controller;

import com.avh.practicas.dashboard.dto.AlertaDto;
import com.avh.practicas.dashboard.dto.DashboardDto;
import com.avh.practicas.dashboard.dto.FiltrosResponse;
import com.avh.practicas.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public DashboardDto getDashboard() {
        return dashboardService.getDashboard();
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