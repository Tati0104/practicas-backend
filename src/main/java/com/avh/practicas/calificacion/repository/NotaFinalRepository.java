package com.avh.practicas.calificacion.repository;

import com.avh.practicas.calificacion.entity.NotaFinal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad NotaFinal.
 */
@Repository
public interface NotaFinalRepository extends JpaRepository<NotaFinal, Long> {
    Optional<NotaFinal> findByInstanciaPracticaId(Long instanciaPracticaId);
}
