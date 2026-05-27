package com.avh.practicas.empresa.service;

import com.avh.practicas.empresa.entity.Empresa;
import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.empresa.repository.EmpresaRepository;
import com.avh.practicas.empresa.repository.TutorEmpresarialRepository;
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
public class TutorEmpresarialServiceImpl implements TutorEmpresarialService {

    private final TutorEmpresarialRepository tutorEmpresarialRepository;
    private final EmpresaRepository empresaRepository;

    @Override
    @Transactional
    public TutorEmpresarial registrar(TutorEmpresarial tutor) {
        if (tutorEmpresarialRepository.existsByCorreo(tutor.getCorreo())) {
            throw new NegocioException("Ya existe un tutor registrado con el correo: " + tutor.getCorreo());
        }

        Empresa empresa = empresaRepository.findById(tutor.getEmpresa().getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la empresa con id: " + tutor.getEmpresa().getId()));

        if (!empresa.getActivo()) {
            throw new NegocioException("No se puede registrar un tutor bajo una empresa inactiva.");
        }

        tutor.setEmpresa(empresa);
        tutor.setActivo(true);
        return tutorEmpresarialRepository.save(tutor);
    }

    @Override
    @Transactional
    public TutorEmpresarial editar(Long id, TutorEmpresarial tutorActualizado) {
        TutorEmpresarial tutorExistente = tutorEmpresarialRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el tutor con id: " + id));

        if (tutorEmpresarialRepository.existsByCorreoAndIdNot(tutorActualizado.getCorreo(), id)) {
            throw new NegocioException("Ya existe otro tutor registrado con el correo: " + tutorActualizado.getCorreo());
        }

        Empresa empresa = empresaRepository.findById(tutorActualizado.getEmpresa().getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la empresa con id: " + tutorActualizado.getEmpresa().getId()));

        tutorExistente.setNombre(tutorActualizado.getNombre());
        tutorExistente.setCargo(tutorActualizado.getCargo());
        tutorExistente.setCorreo(tutorActualizado.getCorreo());
        tutorExistente.setTelefono(tutorActualizado.getTelefono());
        tutorExistente.setUsuarioId(tutorActualizado.getUsuarioId());
        tutorExistente.setEmpresa(empresa);

        if (tutorActualizado.getActivo() != null) {
            tutorExistente.setActivo(tutorActualizado.getActivo());
        }

        return tutorEmpresarialRepository.save(tutorExistente);
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        TutorEmpresarial tutor = tutorEmpresarialRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el tutor con id: " + id));

        tutor.setActivo(false);
        tutorEmpresarialRepository.save(tutor);
    }

    @Override
    @Transactional
    public void activar(Long id) {
        TutorEmpresarial tutor = tutorEmpresarialRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el tutor con id: " + id));

        if (!tutor.getEmpresa().getActivo()) {
            throw new NegocioException("No se puede activar el tutor porque la empresa asociada está inactiva.");
        }

        tutor.setActivo(true);
        tutorEmpresarialRepository.save(tutor);
    }

    @Override
    public Optional<TutorEmpresarial> obtenerPorId(Long id) {
        return tutorEmpresarialRepository.findById(id);
    }

    @Override
    public List<TutorEmpresarial> obtenerPorEmpresa(Long empresaId) {
        return tutorEmpresarialRepository.findByEmpresaId(empresaId);
    }

    @Override
    public List<TutorEmpresarial> obtenerActivosPorEmpresa(Long empresaId) {
        return tutorEmpresarialRepository.findByEmpresaIdAndActivoTrue(empresaId);
    }
}
