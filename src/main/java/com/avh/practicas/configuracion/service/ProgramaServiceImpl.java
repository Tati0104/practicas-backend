package com.avh.practicas.configuracion.service;

import com.avh.practicas.configuracion.entity.Facultad;
import com.avh.practicas.configuracion.entity.Programa;
import com.avh.practicas.configuracion.repository.FacultadRepository;
import com.avh.practicas.configuracion.repository.ProgramaRepository;
import com.avh.practicas.estudiante.repository.EstudianteRepository;
import com.avh.practicas.estudiante.repository.InstanciaPracticaRepository;
import com.avh.practicas.estudiante.entity.EstadoPractica;
import com.avh.practicas.shared.exception.NegocioException;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgramaServiceImpl implements ProgramaService {

    private final ProgramaRepository programaRepository;
    private final FacultadRepository facultadRepository;
    private final EstudianteRepository estudianteRepository;
    private final InstanciaPracticaRepository instanciaPracticaRepository;

    @Override
    @Transactional
    public Programa crear(Programa programa) {
        Facultad facultad = facultadRepository.findById(programa.getFacultad().getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la facultad con id: " + programa.getFacultad().getId()));

        if (!facultad.getActivo()) {
            throw new NegocioException("No se puede crear un programa bajo una facultad inactiva.");
        }

        if (programaRepository.existsByNombreIgnoreCaseAndFacultadId(programa.getNombre(), facultad.getId())) {
            throw new NegocioException("Ya existe un programa con el nombre '" + programa.getNombre() + "' en la facultad seleccionada.");
        }

        programa.setFacultad(facultad);
        programa.setActivo(true);
        return programaRepository.save(programa);
    }

    @Override
    @Transactional
    public Programa editar(Long id, Programa programaActualizado) {
        Programa programaExistente = programaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el programa con id: " + id));

        Facultad facultad = facultadRepository.findById(programaActualizado.getFacultad().getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la facultad con id: " + programaActualizado.getFacultad().getId()));

        if (programaRepository.existsByNombreIgnoreCaseAndFacultadIdAndIdNot(programaActualizado.getNombre(), facultad.getId(), id)) {
            throw new NegocioException("Ya existe otro programa con el nombre '" + programaActualizado.getNombre() + "' en la facultad seleccionada.");
        }

        programaExistente.setNombre(programaActualizado.getNombre());
        programaExistente.setFacultad(facultad);
        programaExistente.setTotalPracticas(programaActualizado.getTotalPracticas());
        
        if (programaActualizado.getActivo() != null) {
            if (!programaActualizado.getActivo()) {
                validarDesactivacion(id);
            }
            programaExistente.setActivo(programaActualizado.getActivo());
        }

        return programaRepository.save(programaExistente);
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        Programa programa = programaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el programa con id: " + id));

        validarDesactivacion(id);
        programa.setActivo(false);
        programaRepository.save(programa);
    }

    @Override
    @Transactional
    public void activar(Long id) {
        Programa programa = programaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el programa con id: " + id));
        
        if (!programa.getFacultad().getActivo()) {
            throw new NegocioException("No se puede activar el programa porque la facultad asociada está inactiva.");
        }

        programa.setActivo(true);
        programaRepository.save(programa);
    }

    @Override
    public List<Programa> obtenerTodos() {
        return programaRepository.findAll();
    }

    @Override
    public Optional<Programa> obtenerPorId(Long id) {
        return programaRepository.findById(id);
    }

    @Override
    public List<Programa> obtenerPorFacultad(Long facultadId) {
        return programaRepository.findByFacultadId(facultadId);
    }

    @Override
    public List<Programa> obtenerActivosPorFacultad(Long facultadId) {
        return programaRepository.findByFacultadIdAndActivoTrue(facultadId);
    }

    private void validarDesactivacion(Long id) {
        // 1. Validar que no existan estudiantes en el programa
        if (estudianteRepository.existsByProgramaId(id)) {
            throw new NegocioException("No se puede desactivar el programa porque tiene estudiantes asociados.");
        }

        // 2. Validar que no existan prácticas activas
        List<EstadoPractica> estadosActivos = List.of(EstadoPractica.ASIGNADA_PENDIENTE_INICIO, EstadoPractica.EN_CURSO);
        if (instanciaPracticaRepository.existsByExpedienteEstudianteProgramaIdAndEstadoIn(id, estadosActivos)) {
            throw new NegocioException("No se puede desactivar el programa porque tiene prácticas empresariales activas en curso.");
        }
    }
}
