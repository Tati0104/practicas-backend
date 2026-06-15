package com.avh.practicas.estudiante.repository;

import com.avh.practicas.estudiante.entity.DocumentoEstudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentoEstudianteRepository extends JpaRepository<DocumentoEstudiante, Long> {
    List<DocumentoEstudiante> findByEstudianteId(Long estudianteId);
}
