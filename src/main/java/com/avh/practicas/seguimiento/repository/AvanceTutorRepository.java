package com.avh.practicas.seguimiento.repository;

import com.avh.practicas.seguimiento.entity.AvanceTutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio JPA para la entidad AvanceTutor.
 */
@Repository
public interface AvanceTutorRepository extends JpaRepository<AvanceTutor, Long> {
    List<AvanceTutor> findByInstanciaPracticaId(Long instanciaPracticaId);
    List<AvanceTutor> findByInstanciaPracticaIdAndCorte(Long instanciaPracticaId, Integer corte);

    @Query("""
            SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
            FROM AvanceTutor a
            WHERE a.instanciaPractica.id = :practicaId AND a.fecha >= :desde
            """)
    boolean existsByPracticaIdAndFechaDesde(
            @Param("practicaId") Long practicaId,
            @Param("desde") LocalDateTime desde
    );
}
