package com.avh.practicas.reporte.controller;

import com.avh.practicas.reporte.dto.ExportacionReporteResponse;
import com.avh.practicas.reporte.dto.ExportarReporteRequest;
import com.avh.practicas.reporte.dto.ReporteResumenDto;
import com.avh.practicas.reporte.service.ReporteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller S6 para reportes.
 * Endpoints reales: /reportes/resumen, /reportes/exportar y /reportes/exportar/estado/{jobId}.
 */
@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService service;

    @GetMapping("/resumen")
    public ReporteResumenDto resumen() {
        return service.resumen();
    }

    @PostMapping("/exportar")
    public ExportacionReporteResponse exportar(@Valid @RequestBody ExportarReporteRequest request) {
        return service.exportar(request);
    }

    @GetMapping("/exportar/estado/{jobId}")
    public ExportacionReporteResponse estado(@PathVariable Long jobId) {
        return service.estado(jobId);
    }
}
