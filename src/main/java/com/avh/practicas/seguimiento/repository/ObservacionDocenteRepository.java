package com.avh.practicas.seguimiento.repository;

import com.avh.practicas.seguimiento.entity.ObservacionDocente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la entidad ObservacionDocente.
 */
@Repository
public interface ObservacionDocenteRepository extends JpaRepository<ObservacionDocente, Long> {
    List<ObservacionDocente> findByInstanciaPracticaId(Long instanciaPracticaId);
    List<ObservacionDocente> findByInstanciaPracticaIdAndCorte(Long instanciaPracticaId, Integer corte);
}
