package com.avh.practicas.estudiante.repository;

import com.avh.practicas.estudiante.entity.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, Long>, JpaSpecificationExecutor<Estudiante> {
    boolean existsByProgramaId(Long programaId);
    boolean existsByIdentificacion(String identificacion);
    boolean existsByCorreo(String correo);
    Optional<Estudiante> findByIdentificacion(String identificacion);
    Optional<Estudiante> findByCorreo(String correo);

    @Query("SELECT e FROM Estudiante e WHERE LOWER(e.correo) = LOWER(:correo)")
    Optional<Estudiante> findByCorreoIgnoreCase(@Param("correo") String correo);

    Optional<Estudiante> findByUsuario_Id(Long usuarioId);

    @Query("""
            SELECT DISTINCT e
            FROM Estudiante e
            JOIN e.expediente exp
            JOIN exp.instanciasPractica ip
            WHERE ip.docenteAsesorId = :docenteAsesorId
            """)
    List<Estudiante> findAsignadosADocente(@Param("docenteAsesorId") Long docenteAsesorId);

    @Query("""
            SELECT COUNT(e)
            FROM Estudiante e
            WHERE e.estadoAptitud = com.avh.practicas.estudiante.entity.EstadoAptitud.APTO
              AND NOT EXISTS (
                    SELECT ip.id
                    FROM InstanciaPractica ip
                    JOIN ip.expediente exp
                    WHERE exp.estudiante = e
                      AND ip.estado IN (
                            com.avh.practicas.estudiante.entity.EstadoPractica.EN_CURSO,
                            com.avh.practicas.estudiante.entity.EstadoPractica.ASIGNADA_PENDIENTE_INICIO
                      )
              )
            """)
    long countAptosSinPracticaActiva();
}
