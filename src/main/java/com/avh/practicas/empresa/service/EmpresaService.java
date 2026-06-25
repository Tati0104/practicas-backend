package com.avh.practicas.empresa.service;

import com.avh.practicas.empresa.entity.Empresa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface EmpresaService {
    Empresa registrar(Empresa empresa);
    Empresa editar(Long id, Empresa empresaActualizada);
    void desactivar(Long id, String motivo);
    void activar(Long id);
    Optional<Empresa> obtenerPorId(Long id);
    Optional<Empresa> obtenerPorNit(String nit);
    List<Empresa> obtenerPorPrograma(Long programaId);
    Page<Empresa> listar(String sector, String programa, Boolean activo, Pageable pageable);
}
