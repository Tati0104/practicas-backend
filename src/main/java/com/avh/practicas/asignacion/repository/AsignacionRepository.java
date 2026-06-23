package com.avh.practicas.asignacion.repository;

import com.avh.practicas.asignacion.entity.Asignacion;
import com.avh.practicas.asignacion.entity.EstadoAsignacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AsignacionRepository extends JpaRepository<Asignacion, Long>, JpaSpecificationExecutor<Asignacion> {
    boolean existsByEstudianteIdAndEstadoIn(Long estudianteId, Collection<EstadoAsignacion> estados);
    List<Asignacion> findByEstudianteId(Long estudianteId);
    List<Asignacion> findByVacanteId(Long vacanteId);

    Optional<Asignacion> findFirstByInstanciaPracticaIdAndEstadoNot(
            Long instanciaPracticaId,
            EstadoAsignacion estado
    );

    @Query(value = """
            SELECT a.*
            FROM asignaciones a
            INNER JOIN estudiantes e ON e.id = a.estudiante_id
            INNER JOIN vacantes v ON v.id = a.vacante_id
            LEFT JOIN empresas emp ON emp.id = v.empresa_id
            WHERE a.estado <> 'CANCELADA'
              AND (:estado IS NULL OR a.estado = :estado)
              AND (:programaId IS NULL OR e.programa_id = :programaId)
              AND (:empresaId IS NULL OR v.empresa_id = :empresaId)
              AND (
                    :busqueda IS NULL OR :busqueda = ''
                    OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(e.identificacion) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(v.cargo) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(COALESCE(emp.razon_social, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                  )
              AND (
                    :tutorId IS NULL
                    OR (
                        v.empresa_id = (SELECT empresa_id FROM tutores_empresariales WHERE id = :tutorId)
                        AND (
                            a.instancia_practica_id IS NULL
                            OR EXISTS (
                                SELECT 1 FROM instancias_practica ip
                                WHERE ip.id = a.instancia_practica_id
                                  AND ip.tutor_id = :tutorId
                            )
                        )
                    )
                  )
              AND (:estudianteId IS NULL OR a.estudiante_id = :estudianteId)
            ORDER BY a.fecha_actualizacion DESC NULLS LAST, a.id DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM asignaciones a
            INNER JOIN estudiantes e ON e.id = a.estudiante_id
            INNER JOIN vacantes v ON v.id = a.vacante_id
            LEFT JOIN empresas emp ON emp.id = v.empresa_id
            WHERE a.estado <> 'CANCELADA'
              AND (:estado IS NULL OR a.estado = :estado)
              AND (:programaId IS NULL OR e.programa_id = :programaId)
              AND (:empresaId IS NULL OR v.empresa_id = :empresaId)
              AND (
                    :busqueda IS NULL OR :busqueda = ''
                    OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(e.identificacion) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(v.cargo) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(COALESCE(emp.razon_social, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                  )
              AND (
                    :tutorId IS NULL
                    OR (
                        v.empresa_id = (SELECT empresa_id FROM tutores_empresariales WHERE id = :tutorId)
                        AND (
                            a.instancia_practica_id IS NULL
                            OR EXISTS (
                                SELECT 1 FROM instancias_practica ip
                                WHERE ip.id = a.instancia_practica_id
                                  AND ip.tutor_id = :tutorId
                            )
                        )
                    )
                  )
              AND (:estudianteId IS NULL OR a.estudiante_id = :estudianteId)
            """,
            nativeQuery = true)
    Page<Asignacion> buscarVinculaciones(
            @Param("busqueda") String busqueda,
            @Param("programaId") Long programaId,
            @Param("empresaId") Long empresaId,
            @Param("estado") String estado,
            @Param("tutorId") Long tutorId,
            @Param("estudianteId") Long estudianteId,
            Pageable pageable
    );

    long countByEstadoNot(EstadoAsignacion estado);
}
