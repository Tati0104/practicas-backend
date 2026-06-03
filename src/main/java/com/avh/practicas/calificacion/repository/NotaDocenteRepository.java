package com.avh.practicas.calificacion.repository;

import com.avh.practicas.calificacion.entity.NotaDocente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad NotaDocente.
 */
@Repository
public interface NotaDocenteRepository extends JpaRepository<NotaDocente, Long> {
    List<NotaDocente> findByInstanciaPracticaId(Long instanciaPracticaId);
    Optional<NotaDocente> findByInstanciaPracticaIdAndCorte(Long instanciaPracticaId, Integer corte);
}
