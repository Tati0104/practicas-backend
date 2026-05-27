package com.avh.practicas.estudiante.repository;

import com.avh.practicas.estudiante.entity.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, Long>, JpaSpecificationExecutor<Estudiante> {
    boolean existsByProgramaId(Long programaId);
    boolean existsByIdentificacion(String identificacion);
    boolean existsByCorreo(String correo);
    Optional<Estudiante> findByIdentificacion(String identificacion);
    Optional<Estudiante> findByCorreo(String correo);
}
