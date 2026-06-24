package com.avh.practicas.estudiante.service;

import com.avh.practicas.estudiante.dto.EstudianteDto;
import com.avh.practicas.estudiante.entity.EstadoAptitud;
import com.avh.practicas.estudiante.entity.Estudiante;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface EstudianteService {
    Estudiante registrar(EstudianteDto dto);
    Estudiante editar(Long id, EstudianteDto dto);
    Estudiante marcarApto(Long id);
    Estudiante marcarApto(Long id, Integer numeroPractica);
    Estudiante marcarNoApto(Long id, String motivo);
    Optional<Estudiante> obtenerPorId(Long id);
    Optional<Estudiante> obtenerPorIdentificacion(String identificacion);
    Page<Estudiante> listar(String programa, String facultad, EstadoAptitud aptitud, String estadoPractica, String busqueda, Pageable pageable);
    void importar(List<Estudiante> estudiantes);
    Estudiante guardar(Estudiante estudiante);
    void eliminar(Long id);
}
