package com.avh.practicas.vinculacion.repository.seguimiento;

import com.avh.practicas.vinculacion.entity.seguimiento.ObservacionDocente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ObservacionDocenteRepository extends JpaRepository<ObservacionDocente, Long> {

    @Query("""
            SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END
            FROM ObservacionDocente o
            WHERE o.instanciaPracticaId = :practicaId AND o.fecha >= :desde
            """)
    boolean existsByPracticaIdAndFechaDesde(
            @Param("practicaId") Long practicaId,
            @Param("desde") LocalDateTime desde
    );
}
