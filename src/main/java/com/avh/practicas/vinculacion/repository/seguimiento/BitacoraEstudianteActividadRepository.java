package com.avh.practicas.vinculacion.repository.seguimiento;

import com.avh.practicas.vinculacion.entity.EntradaTableroSeguimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface BitacoraEstudianteActividadRepository extends JpaRepository<EntradaTableroSeguimiento, Long> {

    @Query("""
            SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
            FROM EntradaTableroSeguimiento b
            WHERE b.instanciaPracticaId = :practicaId AND b.fecha >= :desde
            """)
    boolean existsByPracticaIdAndFechaDesde(
            @Param("practicaId") Long practicaId,
            @Param("desde") LocalDateTime desde
    );
}
