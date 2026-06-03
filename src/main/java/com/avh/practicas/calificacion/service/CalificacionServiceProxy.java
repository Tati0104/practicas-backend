package com.avh.practicas.calificacion.service;

import com.avh.practicas.calificacion.dto.NotaFinalRequest;
import com.avh.practicas.calificacion.dto.NotaRequest;
import com.avh.practicas.calificacion.dto.ResumenCalificacionesResponse;
import com.avh.practicas.calificacion.entity.NotaDocente;
import com.avh.practicas.calificacion.entity.NotaFinal;
import com.avh.practicas.calificacion.entity.NotaTutor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Proxy de seguridad para el servicio de calificaciones.
 * Mantiene la uniformidad en el diseño de proxies y expone los servicios de notas.
 */
@Service
@Primary
public class CalificacionServiceProxy implements CalificacionService {

    private final CalificacionService realService;

    public CalificacionServiceProxy(@Qualifier("calificacionServiceImpl") CalificacionService realService) {
        this.realService = realService;
    }

    @Override
    public NotaDocente registrarNotaDocente(Long practicaId, Long docenteId, Integer corte, NotaRequest request) {
        return realService.registrarNotaDocente(practicaId, docenteId, corte, request);
    }

    @Override
    public NotaTutor registrarNotaTutor(Long practicaId, Long tutorId, Integer corte, NotaRequest request) {
        return realService.registrarNotaTutor(practicaId, tutorId, corte, request);
    }

    @Override
    public NotaFinal registrarNotaFinal(Long practicaId, Long coordinadorId, NotaFinalRequest request) {
        return realService.registrarNotaFinal(practicaId, coordinadorId, request);
    }

    @Override
    public ResumenCalificacionesResponse obtenerResumen(Long practicaId) {
        return realService.obtenerResumen(practicaId);
    }
}
