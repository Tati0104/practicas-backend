package com.avh.practicas.vinculacion.alerta.service;

import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import com.avh.practicas.vinculacion.alerta.*;
import com.avh.practicas.vinculacion.alerta.dto.AlertaVista;
import com.avh.practicas.vinculacion.alerta.dto.CrearAlertaRequest;
import com.avh.practicas.vinculacion.alerta.entity.AlertaSistema;
import com.avh.practicas.vinculacion.alerta.repository.AlertaSistemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AlertaServiceImpl implements AlertaService {

    private final AlertaSistemaRepository repository;
    private final AlertaFabricaDecorator fabricaDecorator;
    private final AlertaOrdenador ordenador;

    @Override
    @Transactional
    public List<AlertaVista> listarActivas(Map<String, String> contextoAutoResolucion) {
        evaluarAutoResolucionGlobal(contextoAutoResolucion);

        List<Alerta> decoradas = new ArrayList<>();
        for (AlertaSistema entidad : repository.findByResueltaFalseOrderByFechaDesc()) {
            decoradas.add(fabricaDecorator.crearDesdeEntidad(entidad));
        }

        return ordenador.ordenar(decoradas).stream()
                .map(Alerta::mostrar)
                .toList();
    }

    @Override
    @Transactional
    public AlertaVista crear(CrearAlertaRequest request) {
        Alerta decorada = fabricaDecorator.crearNueva(
                request.mensaje(),
                request.prioritaria(),
                request.urlAccion(),
                request.nombreModulo(),
                request.condicionResolucion()
        );

        AlertaVista vista = decorada.mostrar();
        AlertaSistema entidad = repository.save(AlertaSistema.builder()
                .mensaje(request.mensaje())
                .prioritaria(request.prioritaria())
                .urlAccion(request.urlAccion())
                .nombreModulo(request.nombreModulo())
                .condicionResolucion(request.condicionResolucion())
                .resuelta(false)
                .leida(false)
                .build());

        return new AlertaVista(
                entidad.getId(),
                vista.mensaje(),
                vista.resuelta(),
                vista.prioritaria(),
                vista.urlAccion(),
                vista.nombreModulo(),
                vista.condicionResolucion(),
                entidad.getFecha(),
                vista.fechaArchivado()
        );
    }

    @Override
    @Transactional
    public AlertaVista resolver(Long id) {
        AlertaSistema entidad = obtenerEntidad(id);
        Alerta decorada = fabricaDecorator.crearDesdeEntidad(entidad);
        decorada.resolver();
        sincronizarEntidad(entidad, decorada);
        return decorada.mostrar();
    }

    @Override
    @Transactional
    public void evaluarAutoResolucionGlobal(Map<String, String> contexto) {
        Map<String, String> ctx = contexto != null ? contexto : Map.of();

        for (AlertaSistema entidad : repository.findByResueltaFalseOrderByFechaDesc()) {
            if (!StringUtils.hasText(entidad.getCondicionResolucion())) {
                continue;
            }
            Alerta decorada = fabricaDecorator.crearDesdeEntidad(entidad);
            DecoradorAutoResolucion auto = buscarAutoResolucion(decorada);
            if (auto != null && auto.intentarAutoResolucion(ctx)) {
                sincronizarEntidad(entidad, decorada);
            }
        }
    }

    private DecoradorAutoResolucion buscarAutoResolucion(Alerta alerta) {
        Alerta actual = alerta;
        while (actual instanceof AlertaDecorator decorator) {
            if (actual instanceof DecoradorAutoResolucion auto) {
                return auto;
            }
            actual = decorator.getEnvuelta();
        }
        return null;
    }

    private void sincronizarEntidad(AlertaSistema entidad, Alerta decorada) {
        AlertaVista vista = decorada.mostrar();
        entidad.setResuelta(vista.resuelta());
        entidad.setLeida(vista.resuelta());
        entidad.setFechaArchivado(extraerFechaArchivado(decorada));
        repository.save(entidad);
    }

    private java.time.LocalDateTime extraerFechaArchivado(Alerta decorada) {
        Alerta actual = decorada;
        while (actual instanceof AlertaDecorator decorator) {
            if (actual instanceof DecoradorArchivable archivable) {
                return archivable.getFechaArchivado();
            }
            actual = decorator.getEnvuelta();
        }
        return null;
    }

    private AlertaSistema obtenerEntidad(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Alerta no encontrada: " + id));
    }
}
