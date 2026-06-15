package com.avh.practicas.estudiante.service;

import com.avh.practicas.configuracion.entity.CatalogoPractica;
import com.avh.practicas.configuracion.entity.Programa;
import com.avh.practicas.configuracion.repository.CatalogoPracticaRepository;
import com.avh.practicas.configuracion.repository.ProgramaRepository;
import com.avh.practicas.estudiante.dto.EstudianteDto;
import com.avh.practicas.estudiante.entity.EstadoAptitud;
import com.avh.practicas.estudiante.entity.EstadoPractica;
import com.avh.practicas.estudiante.entity.Estudiante;
import com.avh.practicas.estudiante.entity.Expediente;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.estudiante.repository.EstudianteRepository;
import com.avh.practicas.estudiante.repository.EstudianteSpecification;
import com.avh.practicas.estudiante.repository.ExpedienteRepository;
import com.avh.practicas.estudiante.repository.InstanciaPracticaRepository;
import com.avh.practicas.shared.exception.CatalogoPracticaNoEncontradaException;
import com.avh.practicas.shared.exception.NegocioException;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import com.avh.practicas.shared.pattern.observer.Observador;
import com.avh.practicas.shared.pattern.singleton.GestorConfiguracion;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EstudianteServiceImpl implements EstudianteService {

    private final EstudianteRepository estudianteRepository;
    private final ExpedienteRepository expedienteRepository;
    private final InstanciaPracticaRepository instanciaPracticaRepository;
    private final ProgramaRepository programaRepository;
    private final CatalogoPracticaRepository catalogoPracticaRepository;

    @Autowired(required = false)
    private List<Observador> observadoresDisponibles;

    @Override
    @Transactional
    public Estudiante registrar(EstudianteDto dto) {
        if (estudianteRepository.existsByIdentificacion(dto.getIdentificacion())) {
            throw new NegocioException("Ya existe un estudiante registrado con la identificación: " + dto.getIdentificacion());
        }

        if (estudianteRepository.existsByCorreo(dto.getCorreo())) {
            throw new NegocioException("Ya existe un estudiante registrado con el correo: " + dto.getCorreo());
        }

        Programa programa = programaRepository.findById(dto.getProgramaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el programa con id: " + dto.getProgramaId()));

        if (!programa.getActivo()) {
            throw new NegocioException("No se puede registrar un estudiante bajo un programa inactivo.");
        }

        Estudiante estudiante = Estudiante.builder()
                .identificacion(dto.getIdentificacion())
                .nombre(dto.getNombre())
                .correo(dto.getCorreo())
                .telefono(dto.getTelefono())
                .contactoEmergencia(dto.getContactoEmergencia())
                .programa(programa)
                .semestre(dto.getSemestre())
                .creditosAprobados(dto.getCreditosAprobados() != null ? dto.getCreditosAprobados() : 0)
                .promedioAcumulado(dto.getPromedioAcumulado() != null ? dto.getPromedioAcumulado() : 0.0)
                .estadoAptitud(EstadoAptitud.SIN_EVALUAR)
                .build();

        estudiante = estudianteRepository.save(estudiante);

        Expediente expediente = Expediente.builder()
                .estudiante(estudiante)
                .build();
        expedienteRepository.save(expediente);

        estudiante.setExpediente(expediente);
        return estudiante;
    }

    @Override
    @Transactional
    public Estudiante marcarApto(Long id) {
        Estudiante estudiante = estudianteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el estudiante con id: " + id));

        GestorConfiguracion config = GestorConfiguracion.getInstancia();

        if (estudiante.getCreditosAprobados() < config.getCreditosMinimos()) {
            throw new NegocioException("El estudiante no cumple con el número mínimo de créditos requeridos (" 
                    + config.getCreditosMinimos() + " créditos). Tiene: " + estudiante.getCreditosAprobados());
        }

        if (estudiante.getPromedioAcumulado() < config.getPromedioMinimo()) {
            throw new NegocioException("El estudiante no cumple con el promedio acumulado mínimo requerido (" 
                    + config.getPromedioMinimo() + "). Tiene: " + estudiante.getPromedioAcumulado());
        }

        Expediente expediente = estudiante.getExpediente();
        if (expediente == null) {
            throw new NegocioException("El estudiante no posee un expediente asociado.");
        }

        // Re-evaluación NO_APTO → APTO: si ya hay una instancia activa (no terminal),
        // solo actualizar el estado sin crear una nueva instancia de práctica.
        boolean tieneInstanciaActiva = expediente.getInstanciasPractica().stream()
                .anyMatch(ip -> ip.getEstado() == EstadoPractica.ASIGNADA_PENDIENTE_INICIO
                             || ip.getEstado() == EstadoPractica.EN_CURSO);

        if (tieneInstanciaActiva) {
            estudiante.setEstadoAptitud(EstadoAptitud.APTO);
            estudiante = estudianteRepository.save(estudiante);
            if (observadoresDisponibles != null) {
                for (Observador obs : observadoresDisponibles) {
                    estudiante.registrarObservador(obs);
                }
            }
            estudiante.notificarObservadores("ESTUDIANTE_MARCADO_APTO", estudiante);
            return estudiante;
        }

        int siguienteNumeroPractica = expediente.getInstanciasPractica().size() + 1;

        if (siguienteNumeroPractica > 1) {
            int practicaAnteriorNum = siguienteNumeroPractica - 1;
            InstanciaPractica practicaAnterior = expediente.getInstanciasPractica().stream()
                    .filter(ip -> ip.getNumeroPractica().equals(practicaAnteriorNum))
                    .findFirst()
                    .orElseThrow(() -> new NegocioException("No se encontró el registro de la práctica anterior número: " + practicaAnteriorNum));

            if (practicaAnterior.getEstado() != EstadoPractica.COMPLETADA) {
                throw new NegocioException("No se puede registrar la práctica número " + siguienteNumeroPractica 
                        + " porque la práctica anterior (" + practicaAnteriorNum + ") no está completada. Estado actual: " 
                        + practicaAnterior.getEstado());
            }
        }

        // Buscar plantilla en CatalogoPractica (PE-55)
        CatalogoPractica plantilla = catalogoPracticaRepository
                .findByProgramaIdAndNumeroPractica(estudiante.getPrograma().getId(), siguienteNumeroPractica)
                .orElseThrow(() -> new CatalogoPracticaNoEncontradaException("No existe entrada en el catálogo para la práctica número " 
                        + siguienteNumeroPractica + " para el programa del estudiante."));

        if (!plantilla.getActivo()) {
            throw new NegocioException("La plantilla de práctica número " + siguienteNumeroPractica + " está inactiva en el catálogo.");
        }

        // Crear InstanciaPractica
        InstanciaPractica nuevaInstancia = InstanciaPractica.builder()
                .expediente(expediente)
                .numeroPractica(siguienteNumeroPractica)
                .nombre(plantilla.getNombre())
                .materiaNucleo(plantilla.getMateriaNucleo())
                .codigoMateria(plantilla.getCodigoMateria())
                .numCortes(plantilla.getNumCortes())
                .duracionSemanas(plantilla.getDuracionSemanas())
                .estado(EstadoPractica.ASIGNADA_PENDIENTE_INICIO)
                .inmutable(false)
                .build();

        instanciaPracticaRepository.save(nuevaInstancia);
        expediente.getInstanciasPractica().add(nuevaInstancia);

        estudiante.setEstadoAptitud(EstadoAptitud.APTO);
        estudiante = estudianteRepository.save(estudiante);

        // Registrar observadores inyectados por Spring
        if (observadoresDisponibles != null) {
            for (Observador obs : observadoresDisponibles) {
                estudiante.registrarObservador(obs);
            }
        }

        // Disparar evento
        estudiante.notificarObservadores("ESTUDIANTE_MARCADO_APTO", estudiante);

        return estudiante;
    }

    @Override
    @Transactional
    public Estudiante marcarNoApto(Long id, String motivo) {
        Estudiante estudiante = estudianteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el estudiante con id: " + id));

        estudiante.setEstadoAptitud(EstadoAptitud.NO_APTO);
        // Notificar en caso de que existan observadores de no aptitud
        if (observadoresDisponibles != null) {
            for (Observador obs : observadoresDisponibles) {
                estudiante.registrarObservador(obs);
            }
        }
        estudiante.notificarObservadores("ESTUDIANTE_MARCADO_NO_APTO", estudiante);

        return estudianteRepository.save(estudiante);
    }

    @Override
    public Optional<Estudiante> obtenerPorId(Long id) {
        return estudianteRepository.findById(id);
    }

    @Override
    public Optional<Estudiante> obtenerPorIdentificacion(String identificacion) {
        return estudianteRepository.findByIdentificacion(identificacion);
    }

    @Override
    public Page<Estudiante> listar(String programa, String facultad, EstadoAptitud aptitud, String estadoPractica, String busqueda, Pageable pageable) {
        return estudianteRepository.findAll(
                EstudianteSpecification.filtrar(programa, facultad, aptitud, estadoPractica, busqueda),
                pageable
        );
    }

    @Override
    @Transactional
    public void importar(List<Estudiante> estudiantes) {
        for (Estudiante est : estudiantes) {
            if (estudianteRepository.existsByIdentificacion(est.getIdentificacion())) {
                throw new NegocioException("Error de importación: Identificación duplicada: " + est.getIdentificacion());
            }
            if (estudianteRepository.existsByCorreo(est.getCorreo())) {
                throw new NegocioException("Error de importación: Correo electrónico duplicado: " + est.getCorreo());
            }

            Estudiante guardado = estudianteRepository.save(est);
            Expediente exp = Expediente.builder().estudiante(guardado).build();
            expedienteRepository.save(exp);
        }
    }

    @Override
    @Transactional
    public Estudiante guardar(Estudiante estudiante) {
        return estudianteRepository.save(estudiante);
    }
}
