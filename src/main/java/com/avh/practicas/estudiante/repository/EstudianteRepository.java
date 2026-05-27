package com.avh.practicas.estudiante.repository;

import com.avh.practicas.estudiante.entity.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {
    boolean existsByProgramaId(Long programaId);
}
