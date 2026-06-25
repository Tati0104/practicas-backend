package com.avh.practicas.respaldo.service;

import com.avh.practicas.respaldo.dto.RespaldoResponse;

/**
 * Contrato del servicio de respaldo.
 * Permite reemplazar el stub por una implementacion completa sin cambiar el controller.
 */
public interface IRespaldoService {
    RespaldoResponse generarRespaldo();
    RespaldoResponse consultarEstado(Long jobId);
}
