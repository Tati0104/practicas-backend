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

    @Query("""
            SELECT ip FROM InstanciaPractica ip
            JOIN FETCH ip.expediente e
            JOIN FETCH e.estudiante est
            JOIN FETCH est.programa
            WHERE ip.id = :id
            """)
    Optional<InstanciaPractica> findByIdWithExpedienteAndEstudiante(@Param("id") Long id);

    Optional<InstanciaPractica> findFirstByExpedienteEstudianteIdAndEstadoOrderByNumeroPracticaDesc(
            Long estudianteId,
            EstadoPractica estado
    );

    boolean existsByExpedienteEstudianteProgramaIdAndEstadoIn(Long programaId, List<EstadoPractica> estados);
    boolean existsByExpedienteEstudianteProgramaIdAndNumeroPracticaAndEstadoIn(Long programaId, Integer numeroPractica, List<EstadoPractica> estados);

    long countByEstado(EstadoPractica estado);

    long countByDocenteAsesorIdAndEstado(Long docenteAsesorId, EstadoPractica estado);

    long countByTutorIdAndEstado(Long tutorId, EstadoPractica estado);

    @Query("""
            SELECT CASE WHEN COUNT(ip) > 0 THEN true ELSE false END
            FROM InstanciaPractica ip
            JOIN ip.expediente e
            WHERE e.estudiante.id = :estudianteId
              AND ip.estado IN :estados
            """)
    boolean existsByExpedienteEstudianteIdAndEstadoIn(
            @Param("estudianteId") Long estudianteId,
            @Param("estados") List<EstadoPractica> estados
    );

    @Query("""
            SELECT CASE WHEN COUNT(ip) > 0 THEN true ELSE false END
            FROM InstanciaPractica ip
            JOIN ip.expediente e
            WHERE e.estudiante.id = :estudianteId
            """)
    boolean existsByExpedienteEstudianteId(@Param("estudianteId") Long estudianteId);

    @Query("""
            SELECT COUNT(ip)
            FROM InstanciaPractica ip
            WHERE ip.estado = com.avh.practicas.estudiante.entity.EstadoPractica.EN_CURSO
              AND NOT EXISTS (
                    SELECT nf.id FROM NotaFinal nf WHERE nf.instanciaPractica = ip
              )
            """)
    long countPracticasEnCursoSinNotaFinal();

    @Query("""
            SELECT COUNT(ip)
            FROM InstanciaPractica ip
            WHERE ip.estado = com.avh.practicas.estudiante.entity.EstadoPractica.EN_CURSO
              AND ip.docenteAsesorId = :docenteAsesorId
              AND NOT EXISTS (
                    SELECT nf.id FROM NotaFinal nf WHERE nf.instanciaPractica = ip
              )
            """)
    long countPracticasEnCursoSinNotaFinalPorDocente(@Param("docenteAsesorId") Long docenteAsesorId);
}
