package com.avh.practicas.empresa.service;

import com.avh.practicas.empresa.entity.Empresa;
import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.empresa.repository.EmpresaRepository;
import com.avh.practicas.empresa.repository.TutorEmpresarialRepository;
import com.avh.practicas.shared.exception.NegocioException;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import com.avh.practicas.usuario.service.CorreoPersonaService;
import com.avh.practicas.usuario.service.UsuarioService;
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
    private final CorreoPersonaService correoPersonaService;
    private final UsuarioService usuarioService;

    @Override
    @Transactional
    public TutorEmpresarial registrar(TutorEmpresarial tutor) {
        String correo = correoPersonaService.normalizar(tutor.getCorreo());
        correoPersonaService.validarCorreoDisponible(correo, CorreoPersonaService.Exclusiones.ninguna());

        Empresa empresa = empresaRepository.findById(tutor.getEmpresa().getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la empresa con id: " + tutor.getEmpresa().getId()));

        if (!empresa.getActivo()) {
            throw new NegocioException("No se puede registrar un tutor bajo una empresa inactiva.");
        }

        var usuario = usuarioService.crearUsuarioTutorEmpresarial(tutor.getNombre(), correo);

        tutor.setEmpresa(empresa);
        tutor.setCorreo(correo);
        tutor.setNombre(tutor.getNombre().trim());
        tutor.setUsuarioId(usuario.getId());
        tutor.setActivo(true);
        return tutorEmpresarialRepository.save(tutor);
    }

    @Override
    @Transactional
    public TutorEmpresarial editar(Long id, TutorEmpresarial tutorActualizado) {
        TutorEmpresarial tutorExistente = tutorEmpresarialRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el tutor con id: " + id));

        String correo = correoPersonaService.normalizar(tutorActualizado.getCorreo());
        correoPersonaService.validarCorreoDisponible(
                correo,
                new CorreoPersonaService.Exclusiones(
                        tutorExistente.getUsuarioId(),
                        null,
                        tutorExistente.getId(),
                        null
                )
        );

        Empresa empresa = empresaRepository.findById(tutorActualizado.getEmpresa().getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la empresa con id: " + tutorActualizado.getEmpresa().getId()));

        tutorExistente.setNombre(tutorActualizado.getNombre().trim());
        tutorExistente.setCargo(tutorActualizado.getCargo());
        tutorExistente.setCorreo(correo);
        tutorExistente.setTelefono(tutorActualizado.getTelefono());
        tutorExistente.setEmpresa(empresa);

        if (tutorExistente.getUsuarioId() != null) {
            var usuario = usuarioService.obtener(tutorExistente.getUsuarioId());
            usuarioService.sincronizarPerfil(usuario, tutorActualizado.getNombre(), correo);
        }

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
