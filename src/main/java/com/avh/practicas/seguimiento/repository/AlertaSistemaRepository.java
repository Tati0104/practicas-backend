package com.avh.practicas.seguimiento.repository;

import com.avh.practicas.seguimiento.entity.AlertaSistema;
import com.avh.practicas.shared.enums.TipoAlerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad AlertaSistema.
 */
@Repository
public interface AlertaSistemaRepository extends JpaRepository<AlertaSistema, Long> {

    List<AlertaSistema> findByLeidaFalseOrderByFechaDesc();

    List<AlertaSistema> findByResueltaFalseOrderByFechaDesc();

    boolean existsByInstanciaPracticaIdAndTipoAndResueltaFalse(Long instanciaPracticaId, TipoAlerta tipo);

    Optional<AlertaSistema> findFirstByInstanciaPracticaIdAndTipoAndResueltaFalse(
            Long instanciaPracticaId,
            TipoAlerta tipo
    );

    List<AlertaSistema> findByInstanciaPracticaIdAndTipoAndResueltaFalse(
            Long instanciaPracticaId,
            TipoAlerta tipo
    );

    List<AlertaSistema> findByLeidaFalseAndResueltaFalseOrderByPrioritariaDescFechaDesc();

    @org.springframework.data.jpa.repository.Query("""
            SELECT a FROM AlertaSistema a
            WHERE a.leida = false AND a.resuelta = false
            AND a.destinatarioCorreo = :correo
            ORDER BY a.prioritaria DESC, a.fecha DESC
            """)
    List<AlertaSistema> findPendientesPersonales(@org.springframework.data.repository.query.Param("correo") String correo);

    @org.springframework.data.jpa.repository.Query("""
            SELECT a FROM AlertaSistema a
            WHERE a.leida = false AND a.resuelta = false
            AND (
                a.destinatarioCorreo = :correo
                OR (a.destinatarioCorreo IS NULL AND a.instanciaPracticaId IN :practicaIds)
            )
            ORDER BY a.prioritaria DESC, a.fecha DESC
            """)
    List<AlertaSistema> findPendientesParaUsuario(
            @org.springframework.data.repository.query.Param("correo") String correo,
            @org.springframework.data.repository.query.Param("practicaIds") java.util.Collection<Long> practicaIds
    );
}
