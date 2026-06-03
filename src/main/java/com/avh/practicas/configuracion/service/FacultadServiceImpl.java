package com.avh.practicas.configuracion.service;

import com.avh.practicas.configuracion.entity.Facultad;
import com.avh.practicas.configuracion.repository.FacultadRepository;
import com.avh.practicas.configuracion.repository.ProgramaRepository;
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
public class FacultadServiceImpl implements FacultadService {

    private final FacultadRepository facultadRepository;
    private final ProgramaRepository programaRepository;

    @Override
    @Transactional
    public Facultad crear(Facultad facultad) {
        if (facultadRepository.existsByNombreIgnoreCase(facultad.getNombre())) {
            throw new NegocioException("Ya existe una facultad con el nombre: " + facultad.getNombre());
        }
        facultad.setActivo(true);
        return facultadRepository.save(facultad);
    }

    @Override
    @Transactional
    public Facultad editar(Long id, Facultad facultadActualizada) {
        Facultad facultadExistente = facultadRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la facultad con id: " + id));

        if (facultadRepository.existsByNombreIgnoreCaseAndIdNot(facultadActualizada.getNombre(), id)) {
            throw new NegocioException("Ya existe otra facultad con el nombre: " + facultadActualizada.getNombre());
        }

        facultadExistente.setNombre(facultadActualizada.getNombre());
        if (facultadActualizada.getActivo() != null) {
            if (!facultadActualizada.getActivo()) {
                // Si se intenta desactivar al editar, validar programas activos
                validarDesactivacion(id);
            }
            facultadExistente.setActivo(facultadActualizada.getActivo());
        }

        return facultadRepository.save(facultadExistente);
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        Facultad facultad = facultadRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la facultad con id: " + id));
        
        validarDesactivacion(id);
        facultad.setActivo(false);
        facultadRepository.save(facultad);
    }

    @Override
    @Transactional
    public void activar(Long id) {
        Facultad facultad = facultadRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la facultad con id: " + id));
        facultad.setActivo(true);
        facultadRepository.save(facultad);
    }

    @Override
    public List<Facultad> obtenerTodas() {
        return facultadRepository.findAll();
    }

    @Override
    public Optional<Facultad> obtenerPorId(Long id) {
        return facultadRepository.findById(id);
    }

    private void validarDesactivacion(Long id) {
        if (programaRepository.existsByFacultadIdAndActivoTrue(id)) {
            throw new NegocioException("No se puede desactivar la facultad porque tiene programas activos asociados.");
        }
    }
}
