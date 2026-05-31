package com.avh.practicas.estudiante.repository;

import com.avh.practicas.estudiante.entity.EstadoPractica;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstanciaPracticaRepository extends JpaRepository<InstanciaPractica, Long> {

    boolean existsByExpedienteEstudianteProgramaIdAndEstadoIn(Long programaId, List<EstadoPractica> estados);

    boolean existsByExpedienteEstudianteProgramaIdAndNumeroPracticaAndEstadoIn(
            Long programaId, Integer numeroPractica, List<EstadoPractica> estados);

    @Query("""
            SELECT ip FROM InstanciaPractica ip
            JOIN FETCH ip.expediente e
            JOIN FETCH e.estudiante
            WHERE ip.id = :id
            """)
    Optional<InstanciaPractica> findByIdWithExpedienteEstudiante(@Param("id") Long id);
}
