package com.avh.practicas.configuracion.service;

import com.avh.practicas.configuracion.entity.CatalogoPractica;
import com.avh.practicas.configuracion.entity.Programa;
import com.avh.practicas.configuracion.repository.CatalogoPracticaRepository;
import com.avh.practicas.configuracion.repository.ProgramaRepository;
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
public class CatalogoPracticaServiceImpl implements CatalogoPracticaService {

    private final CatalogoPracticaRepository catalogoPracticaRepository;
    private final ProgramaRepository programaRepository;
    private final InstanciaPracticaRepository instanciaPracticaRepository;

    @Override
    @Transactional
    public CatalogoPractica crear(CatalogoPractica catalogoPractica) {
        Programa programa = programaRepository.findById(catalogoPractica.getPrograma().getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el programa con id: " + catalogoPractica.getPrograma().getId()));

        if (!programa.getActivo()) {
            throw new NegocioException("No se puede crear un catálogo de práctica bajo un programa inactivo.");
        }

        if (catalogoPracticaRepository.existsByProgramaIdAndNumeroPractica(programa.getId(), catalogoPractica.getNumeroPractica())) {
            throw new NegocioException("Ya existe una plantilla de práctica número " + catalogoPractica.getNumeroPractica() + " para el programa seleccionado.");
        }

        catalogoPractica.setPrograma(programa);
        catalogoPractica.setActivo(true);
        return catalogoPracticaRepository.save(catalogoPractica);
    }

    @Override
    @Transactional
    public CatalogoPractica editar(Long id, CatalogoPractica catalogoPracticaActualizado) {
        CatalogoPractica catalogoExistente = catalogoPracticaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el registro de catálogo de práctica con id: " + id));

        Programa programa = programaRepository.findById(catalogoPracticaActualizado.getPrograma().getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el programa con id: " + catalogoPracticaActualizado.getPrograma().getId()));

        if (catalogoPracticaRepository.existsByProgramaIdAndNumeroPracticaAndIdNot(programa.getId(), catalogoPracticaActualizado.getNumeroPractica(), id)) {
            throw new NegocioException("Ya existe otra plantilla de práctica número " + catalogoPracticaActualizado.getNumeroPractica() + " para el programa seleccionado.");
        }

        catalogoExistente.setNombre(catalogoPracticaActualizado.getNombre());
        catalogoExistente.setMateriaNucleo(catalogoPracticaActualizado.getMateriaNucleo());
        catalogoExistente.setCodigoMateria(catalogoPracticaActualizado.getCodigoMateria());
        catalogoExistente.setNumCortes(catalogoPracticaActualizado.getNumCortes());
        catalogoExistente.setDuracionSemanas(catalogoPracticaActualizado.getDuracionSemanas());
        catalogoExistente.setNumeroPractica(catalogoPracticaActualizado.getNumeroPractica());
        catalogoExistente.setPrograma(programa);

        if (catalogoPracticaActualizado.getActivo() != null) {
            if (!catalogoPracticaActualizado.getActivo()) {
                validarDesactivacion(id);
            }
            catalogoExistente.setActivo(catalogoPracticaActualizado.getActivo());
        }

        return catalogoPracticaRepository.save(catalogoExistente);
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        CatalogoPractica catalogoPractica = catalogoPracticaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el catálogo de práctica con id: " + id));

        validarDesactivacion(id);
        catalogoPractica.setActivo(false);
        catalogoPracticaRepository.save(catalogoPractica);
    }

    @Override
    @Transactional
    public void activar(Long id) {
        CatalogoPractica catalogoPractica = catalogoPracticaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el catálogo de práctica con id: " + id));

        if (!catalogoPractica.getPrograma().getActivo()) {
            throw new NegocioException("No se puede activar el catálogo de práctica porque el programa asociado está inactivo.");
        }

        catalogoPractica.setActivo(true);
        catalogoPracticaRepository.save(catalogoPractica);
    }

    @Override
    public List<CatalogoPractica> obtenerTodos() {
        return catalogoPracticaRepository.findAll();
    }

    @Override
    public Optional<CatalogoPractica> obtenerPorId(Long id) {
        return catalogoPracticaRepository.findById(id);
    }

    @Override
    public List<CatalogoPractica> obtenerPorPrograma(Long programaId) {
        return catalogoPracticaRepository.findByProgramaId(programaId);
    }

    @Override
    public List<CatalogoPractica> obtenerActivosPorPrograma(Long programaId) {
        return catalogoPracticaRepository.findByProgramaIdAndActivoTrue(programaId);
    }

    private void validarDesactivacion(Long id) {
        CatalogoPractica catalogoPractica = catalogoPracticaRepository.findById(id).orElseThrow();
        List<EstadoPractica> estadosActivos = List.of(EstadoPractica.ASIGNADA_PENDIENTE_INICIO, EstadoPractica.EN_CURSO);
        boolean existenInstanciasActivas = instanciaPracticaRepository
                .existsByExpedienteEstudianteProgramaIdAndNumeroPracticaAndEstadoIn(
                        catalogoPractica.getPrograma().getId(),
                        catalogoPractica.getNumeroPractica(),
                        estadosActivos
                );
        if (existenInstanciasActivas) {
            throw new NegocioException("No se puede desactivar esta plantilla de práctica porque existen instancias de práctica activas basadas en ella.");
        }
    }
}
