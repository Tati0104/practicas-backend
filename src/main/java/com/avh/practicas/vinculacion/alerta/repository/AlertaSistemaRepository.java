package com.avh.practicas.vinculacion.alerta.repository;

import com.avh.practicas.vinculacion.alerta.entity.AlertaSistema;
import com.avh.practicas.vinculacion.alerta.entity.TipoAlerta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlertaSistemaRepository extends JpaRepository<AlertaSistema, Long> {

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
