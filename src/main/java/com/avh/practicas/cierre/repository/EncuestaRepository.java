package com.avh.practicas.cierre.repository;

import com.avh.practicas.cierre.entity.Encuesta;
import com.avh.practicas.cierre.entity.TipoEncuesta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Encuesta.
 */
@Repository
public interface EncuestaRepository extends JpaRepository<Encuesta, Long> {
    Optional<Encuesta> findByInstanciaPracticaIdAndTipo(Long instanciaPracticaId, TipoEncuesta tipo);
    List<Encuesta> findByInstanciaPracticaId(Long instanciaPracticaId);
}
