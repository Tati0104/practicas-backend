package com.avh.practicas.cierre.service;

import com.avh.practicas.cierre.entity.Encuesta;
import com.avh.practicas.cierre.entity.TipoEncuesta;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Proxy de seguridad y control para el servicio de encuestas.
 * Mantiene la uniformidad del diseño de capas de la aplicación.
 */
@Service
@Primary
public class EncuestaServiceProxy implements EncuestaService {

    private final EncuestaService realService;

    public EncuestaServiceProxy(@Qualifier("encuestaServiceImpl") EncuestaService realService) {
        this.realService = realService;
    }

    @Override
    public Encuesta crearEncuestaPendiente(Long practicaId, TipoEncuesta tipo) {
        return realService.crearEncuestaPendiente(practicaId, tipo);
    }

    @Override
    public Encuesta guardarBorrador(Long encuestaId, String respuestasJson) {
        return realService.guardarBorrador(encuestaId, respuestasJson);
    }

    @Override
    public Encuesta enviar(Long encuestaId) {
        return realService.enviar(encuestaId);
    }

    @Override
    public void enviarRecordatorio(Long practicaId, TipoEncuesta tipo) {
        realService.enviarRecordatorio(practicaId, tipo);
    }

    @Override
    public boolean isCompleta(Long practicaId, TipoEncuesta tipo) {
        return realService.isCompleta(practicaId, tipo);
    }

    @Override
    public Optional<Encuesta> obtenerPorPracticaYTipo(Long practicaId, TipoEncuesta tipo) {
        return realService.obtenerPorPracticaYTipo(practicaId, tipo);
    }
}
