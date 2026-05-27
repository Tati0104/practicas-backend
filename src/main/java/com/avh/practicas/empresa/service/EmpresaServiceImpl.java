package com.avh.practicas.empresa.service;

import com.avh.practicas.configuracion.entity.CatalogoItem;
import com.avh.practicas.configuracion.repository.CatalogoItemRepository;
import com.avh.practicas.empresa.entity.Empresa;
import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.empresa.repository.EmpresaRepository;
import com.avh.practicas.empresa.repository.EmpresaSpecification;
import com.avh.practicas.empresa.repository.TutorEmpresarialRepository;
import com.avh.practicas.shared.exception.NegocioException;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import com.avh.practicas.vacante.repository.VacanteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmpresaServiceImpl implements EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final TutorEmpresarialRepository tutorEmpresarialRepository;
    private final VacanteRepository vacanteRepository;
    private final CatalogoItemRepository catalogoItemRepository;

    @Override
    @Transactional
    public Empresa registrar(Empresa empresa) {
        if (empresaRepository.existsByNit(empresa.getNit())) {
            throw new NegocioException("Ya existe una empresa registrada con el NIT: " + empresa.getNit());
        }

        CatalogoItem sector = catalogoItemRepository.findById(empresa.getSector().getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el sector económico con id: " + empresa.getSector().getId()));

        empresa.setSector(sector);
        empresa.setActivo(true);
        return empresaRepository.save(empresa);
    }

    @Override
    @Transactional
    public Empresa editar(Long id, Empresa empresaActualizada) {
        Empresa empresaExistente = empresaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la empresa con id: " + id));

        if (empresaRepository.existsByNit(empresaActualizada.getNit()) && !empresaExistente.getNit().equals(empresaActualizada.getNit())) {
            throw new NegocioException("Ya existe otra empresa registrada con el NIT: " + empresaActualizada.getNit());
        }

        CatalogoItem sector = catalogoItemRepository.findById(empresaActualizada.getSector().getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el sector económico con id: " + empresaActualizada.getSector().getId()));

        empresaExistente.setNit(empresaActualizada.getNit());
        empresaExistente.setRazonSocial(empresaActualizada.getRazonSocial());
        empresaExistente.setSector(sector);
        empresaExistente.setDireccion(empresaActualizada.getDireccion());
        empresaExistente.setMunicipio(empresaActualizada.getMunicipio());
        empresaExistente.setTelefono(empresaActualizada.getTelefono());

        if (empresaActualizada.getActivo() != null) {
            if (!empresaActualizada.getActivo()) {
                validarDesactivacion(id);
                desactivarTutoresEnCascada(id);
            }
            empresaExistente.setActivo(empresaActualizada.getActivo());
        }

        return empresaRepository.save(empresaExistente);
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la empresa con id: " + id));

        validarDesactivacion(id);
        desactivarTutoresEnCascada(id);

        empresa.setActivo(false);
        empresaRepository.save(empresa);
    }

    @Override
    @Transactional
    public void activar(Long id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la empresa con id: " + id));

        empresa.setActivo(true);
        empresaRepository.save(empresa);
    }

    @Override
    public Optional<Empresa> obtenerPorId(Long id) {
        return empresaRepository.findById(id);
    }

    @Override
    public Optional<Empresa> obtenerPorNit(String nit) {
        return empresaRepository.findByNit(nit);
    }

    @Override
    public List<Empresa> obtenerPorPrograma(Long programaId) {
        return empresaRepository.findByProgramaId(programaId);
    }

    @Override
    public Page<Empresa> listar(String sector, String programa, Boolean activo, Pageable pageable) {
        return empresaRepository.findAll(
                EmpresaSpecification.filtrar(sector, programa, activo),
                pageable
        );
    }

    private void validarDesactivacion(Long id) {
        // Validar que no tenga vacantes activas (PENDIENTE_APROBACION o ACTIVA) según enums de V5
        boolean tieneVacantesActivas = vacanteRepository.existsByEmpresaIdAndEstado(id, "PENDIENTE_APROBACION")
                || vacanteRepository.existsByEmpresaIdAndEstado(id, "ACTIVA");

        if (tieneVacantesActivas) {
            throw new NegocioException("No se puede desactivar la empresa porque posee vacantes activas (en aprobación o activas).");
        }
    }

    private void desactivarTutoresEnCascada(Long id) {
        // Desactivar todos los tutores asociados en cascada
        List<TutorEmpresarial> tutores = tutorEmpresarialRepository.findByEmpresaId(id);
        for (TutorEmpresarial tutor : tutores) {
            tutor.setActivo(false);
            tutorEmpresarialRepository.save(tutor);
        }
    }
}
