package com.avh.practicas.vinculacion.repository;

import com.avh.practicas.vinculacion.entity.Convenio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConvenioRepository extends JpaRepository<Convenio, Long> {

    Optional<Convenio> findByAsignacionId(Long asignacionId);

    Optional<Convenio> findByInstanciaPracticaId(Long instanciaPracticaId);

    @Query("""
            SELECT COUNT(c)
            FROM Convenio c
            WHERE c.firmaTutorAt IS NULL
              AND c.instanciaPracticaId IN (
                    SELECT ip.id FROM InstanciaPractica ip WHERE ip.tutorId = :tutorId
              )
            """)
    long countPendienteFirmaTutor(@Param("tutorId") Long tutorId);
}
