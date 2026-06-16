package com.avh.practicas.vinculacion.repository;

import com.avh.practicas.estudiante.entity.EstadoPractica;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Consultas de {@link InstanciaPractica} usadas solo por el módulo de vinculación (sin modificar el paquete estudiante).
 */
public interface PracticaVinculacionRepository extends JpaRepository<InstanciaPractica, Long> {

    @Query("""
            SELECT ip FROM InstanciaPractica ip
            JOIN FETCH ip.expediente e
            JOIN FETCH e.estudiante est
            JOIN FETCH est.programa
            WHERE ip.id = :id
            """)
    Optional<InstanciaPractica> findByIdConExpediente(@Param("id") Long id);

    Optional<InstanciaPractica> findFirstByExpedienteEstudianteIdAndEstadoOrderByNumeroPracticaDesc(
            Long estudianteId,
            EstadoPractica estado
    );

    List<InstanciaPractica> findByEstado(EstadoPractica estado);

    @Query("""
            SELECT CASE WHEN COUNT(ip) > 0 THEN true ELSE false END
            FROM InstanciaPractica ip
            JOIN ip.expediente e
            WHERE e.estudiante.id = :estudianteId
              AND ip.tutorId = :tutorId
            """)
    boolean existsByExpedienteEstudianteIdAndTutorId(
            @Param("estudianteId") Long estudianteId,
            @Param("tutorId") Long tutorId
    );
}
