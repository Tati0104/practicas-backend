package com.avh.practicas.vinculacion.repository.seguimiento;

import com.avh.practicas.vinculacion.entity.seguimiento.AvanceTutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AvanceTutorRepository extends JpaRepository<AvanceTutor, Long> {

    @Query("""
            SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
            FROM AvanceTutor a
            WHERE a.instanciaPracticaId = :practicaId AND a.fecha >= :desde
            """)
    boolean existsByPracticaIdAndFechaDesde(
            @Param("practicaId") Long practicaId,
            @Param("desde") LocalDateTime desde
    );
}
