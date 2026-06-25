package com.avh.practicas.seguimiento.repository;

import com.avh.practicas.seguimiento.entity.BitacoraEstudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad BitacoraEstudiante.
 */
@Repository
public interface BitacoraEstudianteRepository extends JpaRepository<BitacoraEstudiante, Long> {
    List<BitacoraEstudiante> findByInstanciaPracticaId(Long instanciaPracticaId);
    List<BitacoraEstudiante> findByInstanciaPracticaIdAndCorte(Long instanciaPracticaId, Integer corte);
    Optional<BitacoraEstudiante> findFirstByInstanciaPracticaIdOrderByFechaDesc(Long instanciaPracticaId);

    @Query("""
            SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
            FROM BitacoraEstudiante b
            WHERE b.instanciaPractica.id = :practicaId AND b.fecha >= :desde
            """)
    boolean existsByPracticaIdAndFechaDesde(
            @Param("practicaId") Long practicaId,
            @Param("desde") LocalDateTime desde
    );
}
