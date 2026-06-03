package com.avh.practicas.seguimiento.repository;

import com.avh.practicas.seguimiento.entity.AlertaSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la entidad AlertaSistema.
 */
@Repository
public interface AlertaSistemaRepository extends JpaRepository<AlertaSistema, Long> {
    List<AlertaSistema> findByLeidaFalseOrderByFechaDesc();
}
