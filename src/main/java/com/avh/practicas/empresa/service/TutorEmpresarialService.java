package com.avh.practicas.empresa.service;

import com.avh.practicas.empresa.entity.TutorEmpresarial;

import java.util.List;
import java.util.Optional;

public interface TutorEmpresarialService {
    TutorEmpresarial registrar(TutorEmpresarial tutor);
    TutorEmpresarial editar(Long id, TutorEmpresarial tutorActualizado);
    void desactivar(Long id);
    void activar(Long id);
    Optional<TutorEmpresarial> obtenerPorId(Long id);
    List<TutorEmpresarial> obtenerPorEmpresa(Long empresaId);
    List<TutorEmpresarial> obtenerActivosPorEmpresa(Long empresaId);
}
