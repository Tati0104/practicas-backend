package com.avh.practicas.cierre.support;

import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.estudiante.repository.InstanciaPracticaRepository;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import com.avh.practicas.vinculacion.repository.DocumentoPracticaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Activa el modo proxy de documentos tras archivar el expediente (PE-43).
 * <p>
 * Las mutaciones de documentos consultan {@code instancias_practica.inmutable}; este paso
 * confirma el cierre documental para todos los archivos de la práctica.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentoProxyActivador {

    private final InstanciaPracticaRepository practicaRepository;
    private final DocumentoPracticaRepository documentoPracticaRepository;

    public void activarParaPractica(Long practicaId) {
        InstanciaPractica practica = practicaRepository.findById(practicaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Práctica no encontrada: " + practicaId));

        if (!Boolean.TRUE.equals(practica.getInmutable())) {
            throw new IllegalStateException(
                    "Debe archivar el expediente (inmutable=true) antes de activar el proxy de documentos.");
        }

        int documentos = documentoPracticaRepository.findByInstanciaPracticaIdOrderByFechaDesc(practicaId).size();
        log.info("Proxy de documentos activo para práctica {} ({} documentos en modo solo lectura)",
                practicaId, documentos);
    }
}
