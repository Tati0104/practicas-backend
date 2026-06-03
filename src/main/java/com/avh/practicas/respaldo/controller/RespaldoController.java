package com.avh.practicas.respaldo.controller;

import com.avh.practicas.respaldo.dto.RespaldoResponse;
import com.avh.practicas.respaldo.service.IRespaldoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller S5 para probar el respaldo stub.
 * Expone la generacion y consulta de estado del job.
 */
@RestController
@RequestMapping("/admin/respaldo")
@RequiredArgsConstructor
public class RespaldoController {

    private final IRespaldoService respaldoService;

    @PostMapping("/generar")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public RespaldoResponse generar() {
        return respaldoService.generarRespaldo();
    }

    @GetMapping("/estado/{jobId}")
    public RespaldoResponse estado(@PathVariable Long jobId) {
        return respaldoService.consultarEstado(jobId);
    }
}
