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
}
