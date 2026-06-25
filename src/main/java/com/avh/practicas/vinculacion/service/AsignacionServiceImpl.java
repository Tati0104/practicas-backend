package com.avh.practicas.vinculacion.service;

import com.avh.practicas.asignacion.entity.Asignacion;
import com.avh.practicas.asignacion.entity.EstadoAsignacion;
import com.avh.practicas.asignacion.repository.AsignacionRepository;
import com.avh.practicas.empresa.entity.Empresa;
import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.empresa.repository.EmpresaRepository;
import com.avh.practicas.empresa.repository.TutorEmpresarialRepository;
import com.avh.practicas.estudiante.entity.Estudiante;
import com.avh.practicas.estudiante.repository.EstudianteRepository;
import com.avh.practicas.shared.exception.NegocioException;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import com.avh.practicas.vacante.entity.Vacante;
import com.avh.practicas.vacante.repository.VacanteRepository;
import com.avh.practicas.vinculacion.dto.AsignacionResponse;
import com.avh.practicas.vinculacion.dto.CancelarAsignacionRequest;
import com.avh.practicas.vinculacion.dto.CrearAsignacionRequest;
import com.avh.practicas.vinculacion.event.EventoAsignacion;
import com.avh.practicas.vinculacion.support.AsignacionObservadorRegistry;
import com.avh.practicas.vinculacion.support.AsignacionSubject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AsignacionServiceImpl implements AsignacionService {

    private final AsignacionRepository asignacionRepository;
    private final EstudianteRepository estudianteRepository;
    private final VacanteRepository vacanteRepository;
    private final EmpresaRepository empresaRepository;
    private final TutorEmpresarialRepository tutorRepository;
    private final AsignacionObservadorRegistry observadorRegistry;

    @Override
    @Transactional
    public AsignacionResponse crear(CrearAsignacionRequest request) {
        Estudiante estudiante = estudianteRepository.findById(request.estudianteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Estudiante no encontrado: " + request.estudianteId()));

        Vacante vacante = vacanteRepository.findById(request.vacanteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Vacante no encontrada: " + request.vacanteId()));

        Asignacion asignacion = asignacionRepository.save(Asignacion.builder()
                .vacanteId(request.vacanteId())
                .estudianteId(request.estudianteId())
                .estado(EstadoAsignacion.ASIGNADA)
                .build());

        dispararEvento(asignacion, EventoAsignacion.NUEVA_ASIGNACION, Map.of(
                "estado", asignacion.getEstado().name()
        ), estudiante, vacante);

        return AsignacionResponse.desde(asignacion);
    }

    @Override
    @Transactional
    public AsignacionResponse cambiarEstado(Long asignacionId, EstadoAsignacion nuevoEstado) {
        Asignacion asignacion = obtenerAsignacion(asignacionId);
        if (asignacion.getEstado() == nuevoEstado) {
            return AsignacionResponse.desde(asignacion);
        }
        if (asignacion.getEstado() == EstadoAsignacion.CANCELADA) {
            throw new NegocioException("No se puede cambiar el estado de una asignación cancelada.");
        }

        EstadoAsignacion estadoAnterior = asignacion.getEstado();
        asignacion.setEstado(nuevoEstado);
        Asignacion guardada = asignacionRepository.save(asignacion);

        ContextoAsignacion contexto = cargarContexto(guardada);
        dispararEvento(guardada, EventoAsignacion.CAMBIO_ESTADO_ASIGNACION, Map.of(
                "estado", nuevoEstado.name(),
                "estado_anterior", estadoAnterior.name()
        ), contexto.estudiante(), contexto.vacante());

        return AsignacionResponse.desde(guardada);
    }

    @Override
    @Transactional
    public AsignacionResponse cancelar(Long asignacionId, CancelarAsignacionRequest request) {
        Asignacion asignacion = obtenerAsignacion(asignacionId);
        if (asignacion.getEstado() == EstadoAsignacion.CANCELADA) {
            throw new NegocioException("La asignación ya está cancelada.");
        }

        EstadoAsignacion estadoAnterior = asignacion.getEstado();
        asignacion.setEstado(EstadoAsignacion.CANCELADA);
        asignacion.setMotivoCancelacion(request.motivo());
        Asignacion guardada = asignacionRepository.save(asignacion);

        ContextoAsignacion contexto = cargarContexto(guardada);
        dispararEvento(guardada, EventoAsignacion.ASIGNACION_CANCELADA, Map.of(
                "estado", EstadoAsignacion.CANCELADA.name(),
                "estado_anterior", estadoAnterior.name(),
                "motivo", request.motivo()
        ), contexto.estudiante(), contexto.vacante());

        return AsignacionResponse.desde(guardada);
    }

    private void dispararEvento(
            Asignacion asignacion,
            String tipoEvento,
            Map<String, Object> extras,
            Estudiante estudiante,
            Vacante vacante
    ) {
        Map<String, Object> datos = construirDatos(asignacion, estudiante, vacante);
        datos.putAll(extras);
        AsignacionSubject subject = new AsignacionSubject(asignacion);
        observadorRegistry.registrarObservadores(subject);
        subject.notificarObservadores(tipoEvento, datos);
    }

    private void dispararEvento(
            Asignacion asignacion,
            String tipoEvento,
            Map<String, Object> extras,
            ContextoAsignacion contexto
    ) {
        dispararEvento(asignacion, tipoEvento, extras, contexto.estudiante(), contexto.vacante());
    }

    private Map<String, Object> construirDatos(Asignacion asignacion, Estudiante estudiante, Vacante vacante) {
        Map<String, Object> datos = new HashMap<>();
        datos.put("asignacionId", asignacion.getId());
        datos.put("estudianteId", estudiante.getId());
        datos.put("nombre_estudiante", estudiante.getNombre());
        datos.put("correo_estudiante", estudiante.getCorreo());
        datos.put("vacanteId", vacante.getId());
        datos.put("cargo_vacante", vacante.getCargo());

        Empresa empresa = empresaRepository.findById(vacante.getEmpresaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Empresa no encontrada: " + vacante.getEmpresaId()));
        datos.put("empresaId", empresa.getId());
        datos.put("empresa", empresa.getRazonSocial());

        Optional<TutorEmpresarial> tutor = tutorRepository.findByEmpresaIdAndActivoTrue(empresa.getId())
                .stream()
                .findFirst();
        tutor.ifPresent(t -> {
            datos.put("tutorId", t.getId());
            datos.put("nombre_tutor", t.getNombre());
            datos.put("correo_tutor", t.getCorreo());
        });

        return datos;
    }

    private ContextoAsignacion cargarContexto(Asignacion asignacion) {
        Estudiante estudiante = estudianteRepository.findById(asignacion.getEstudianteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Estudiante no encontrado"));
        Vacante vacante = vacanteRepository.findById(asignacion.getVacanteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Vacante no encontrada"));
        return new ContextoAsignacion(estudiante, vacante);
    }

    private Asignacion obtenerAsignacion(Long id) {
        return asignacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Asignación no encontrada: " + id));
    }

    private record ContextoAsignacion(Estudiante estudiante, Vacante vacante) {
    }
}
