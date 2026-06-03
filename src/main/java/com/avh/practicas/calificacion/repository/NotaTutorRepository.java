package com.avh.practicas.calificacion.repository;

import com.avh.practicas.calificacion.entity.NotaTutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad NotaTutor.
 */
@Repository
public interface NotaTutorRepository extends JpaRepository<NotaTutor, Long> {
    List<NotaTutor> findByInstanciaPracticaId(Long instanciaPracticaId);
    Optional<NotaTutor> findByInstanciaPracticaIdAndCorte(Long instanciaPracticaId, Integer corte);
}
