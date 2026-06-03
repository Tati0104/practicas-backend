package com.avh.practicas.estudiante.repository;

import com.avh.practicas.estudiante.entity.Expediente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExpedienteRepository extends JpaRepository<Expediente, Long> {
    Optional<Expediente> findByEstudianteId(Long estudianteId);
    Optional<Expediente> findByEstudianteIdentificacion(String identificacion);
}
