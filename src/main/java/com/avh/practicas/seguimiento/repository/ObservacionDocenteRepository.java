package com.avh.practicas.seguimiento.repository;

import com.avh.practicas.seguimiento.entity.ObservacionDocente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio JPA para la entidad ObservacionDocente.
 */
@Repository
public interface ObservacionDocenteRepository extends JpaRepository<ObservacionDocente, Long> {
    List<ObservacionDocente> findByInstanciaPracticaId(Long instanciaPracticaId);
    List<ObservacionDocente> findByInstanciaPracticaIdAndCorte(Long instanciaPracticaId, Integer corte);

    @Query("""
            SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END
            FROM ObservacionDocente o
            WHERE o.instanciaPractica.id = :practicaId AND o.fecha >= :desde
            """)
    boolean existsByPracticaIdAndFechaDesde(
            @Param("practicaId") Long practicaId,
            @Param("desde") LocalDateTime desde
    );
}
