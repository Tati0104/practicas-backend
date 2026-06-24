package com.avh.practicas.estudiante.repository;

import com.avh.practicas.estudiante.entity.Expediente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExpedienteRepository extends JpaRepository<Expediente, Long> {
    Optional<Expediente> findByEstudianteId(Long estudianteId);

    @Query("""
            SELECT e FROM Expediente e
            LEFT JOIN FETCH e.instanciasPractica
            WHERE e.estudiante.id = :estudianteId
            """)
    Optional<Expediente> findByEstudianteIdWithInstancias(@Param("estudianteId") Long estudianteId);

    Optional<Expediente> findByEstudianteIdentificacion(String identificacion);
}
