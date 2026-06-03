package com.avh.practicas.vinculacion.alerta.service;

import com.avh.practicas.shared.exception.NegocioException;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import com.avh.practicas.shared.pattern.singleton.GestorConfiguracion;
import com.avh.practicas.estudiante.entity.EstadoPractica;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.vinculacion.alerta.*;
import com.avh.practicas.vinculacion.alerta.dto.AlertaVista;
import com.avh.practicas.vinculacion.alerta.dto.CrearAlertaRequest;
import com.avh.practicas.vinculacion.alerta.entity.AlertaSistema;
import com.avh.practicas.vinculacion.alerta.entity.TipoAlerta;
import com.avh.practicas.vinculacion.alerta.repository.AlertaSistemaRepository;
import com.avh.practicas.vinculacion.alerta.support.ConsultaActividadPractica;
import com.avh.practicas.vinculacion.alerta.support.DiasHabilesUtil;
import com.avh.practicas.vinculacion.repository.PracticaVinculacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertaServiceImpl implements AlertaService {

    private static final Map<String, String> CONTEXTO_NUEVA_ACTIVIDAD = Map.of("nueva_actividad", "true");

    private final AlertaSistemaRepository repository;
    private final AlertaFabricaDecorator fabricaDecorator;
    private final AlertaOrdenador ordenador;
    private final AlertaDecoradorPorTipo decoradorPorTipo;
    private final PracticaVinculacionRepository practicaRepository;
    private final ConsultaActividadPractica consultaActividad;

    @Override
    @Transactional
    public List<AlertaVista> listarActivas(Map<String, String> contextoAutoResolucion) {
        evaluarAutoResolucionGlobal(contextoAutoResolucion);
        return listarDecoradasActivas();
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

        return vistaConId(entidad, vista);
    }

    @Override
    @Transactional
    public AlertaVista resolver(Long id) {
        return resolverAlerta(id);
    }

    @Override
    @Transactional
    public AlertaVista resolverAlerta(Long id) {
        AlertaSistema entidad = obtenerEntidad(id);
        Alerta decorada = fabricaDecorator.crearDesdeEntidad(entidad);
        decorada.resolver();
        sincronizarEntidad(entidad, decorada);
        return decorada.mostrar();
    }

    @Override
    @Transactional
    public AlertaVista generarAlerta(TipoAlerta tipo, Long practicaId) {
        validarPracticaExiste(practicaId);

        if (repository.existsByInstanciaPracticaIdAndTipoAndResueltaFalse(practicaId, tipo)) {
            return repository.findFirstByInstanciaPracticaIdAndTipoAndResueltaFalse(practicaId, tipo)
                    .map(entidad -> fabricaDecorator.crearDesdeEntidad(entidad).mostrar())
                    .orElseThrow(() -> new NegocioException("La alerta activa ya no está disponible"));
        }

        int umbralDias = GestorConfiguracion.getInstancia().getConfig().getUmbralInactividadDias();
        String mensaje = construirMensaje(tipo, practicaId, umbralDias);

        AlertaDecoradorPorTipo.ConfiguracionDecoradores config = decoradorPorTipo.configuracion(tipo, practicaId);
        Alerta base = new AlertaBase(null, mensaje, LocalDateTime.now(), false);
        Alerta decorada = decoradorPorTipo.aplicarDecoradores(base, config);
        AlertaVista vista = decorada.mostrar();

        AlertaSistema entidad = repository.save(AlertaSistema.builder()
                .mensaje(mensaje)
                .tipo(tipo)
                .instanciaPracticaId(practicaId)
                .prioritaria(config.prioritaria())
                .urlAccion(config.urlAccion())
                .nombreModulo(config.nombreModulo())
                .condicionResolucion(config.condicionResolucion())
                .resuelta(false)
                .leida(false)
                .build());

        log.info("Alerta {} generada para práctica {}", tipo, practicaId);
        return vistaConId(entidad, vista);
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

    @Override
    @Transactional
    public void evaluarInactividad() {
        int umbralDias = GestorConfiguracion.getInstancia().getConfig().getUmbralInactividadDias();
        LocalDateTime desde = DiasHabilesUtil.inicioHaceDiasHabiles(umbralDias);

        List<InstanciaPractica> practicasEnCurso = practicaRepository.findByEstado(EstadoPractica.EN_CURSO);

        for (InstanciaPractica practica : practicasEnCurso) {
            Long practicaId = practica.getId();
            boolean hayActividad = consultaActividad.tieneActividadReciente(practicaId, desde);

            if (hayActividad) {
                autoResolverInactividadPorNuevaActividad(practicaId);
            } else if (!repository.existsByInstanciaPracticaIdAndTipoAndResueltaFalse(
                    practicaId, TipoAlerta.INACTIVIDAD)) {
                generarAlerta(TipoAlerta.INACTIVIDAD, practicaId);
            }
        }
    }

    private void autoResolverInactividadPorNuevaActividad(Long practicaId) {
        List<AlertaSistema> alertas = repository.findByInstanciaPracticaIdAndTipoAndResueltaFalse(
                practicaId, TipoAlerta.INACTIVIDAD);

        for (AlertaSistema entidad : alertas) {
            Alerta decorada = fabricaDecorator.crearDesdeEntidad(entidad);
            DecoradorAutoResolucion auto = buscarAutoResolucion(decorada);
            if (auto != null && auto.intentarAutoResolucion(CONTEXTO_NUEVA_ACTIVIDAD)) {
                sincronizarEntidad(entidad, decorada);
                log.info("Alerta de inactividad {} auto-resuelta por nueva actividad", entidad.getId());
            }
        }
    }

    private List<AlertaVista> listarDecoradasActivas() {
        List<Alerta> decoradas = new ArrayList<>();
        for (AlertaSistema entidad : repository.findByResueltaFalseOrderByFechaDesc()) {
            decoradas.add(fabricaDecorator.crearDesdeEntidad(entidad));
        }
        return ordenador.ordenar(decoradas).stream()
                .map(Alerta::mostrar)
                .toList();
    }

    private String construirMensaje(TipoAlerta tipo, Long practicaId, int umbralDias) {
        return switch (tipo) {
            case INACTIVIDAD -> "Inactividad en la práctica " + practicaId
                    + ": sin observaciones, avances ni bitácora en los últimos "
                    + umbralDias + " días hábiles.";
        };
    }

    private void validarPracticaExiste(Long practicaId) {
        if (!practicaRepository.existsById(practicaId)) {
            throw new RecursoNoEncontradoException("Práctica no encontrada: " + practicaId);
        }
    }

    private AlertaVista vistaConId(AlertaSistema entidad, AlertaVista vista) {
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

    private LocalDateTime extraerFechaArchivado(Alerta decorada) {
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
