package com.avh.practicas.seguimiento.repository;

import com.avh.practicas.seguimiento.entity.AvanceTutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la entidad AvanceTutor.
 */
@Repository
public interface AvanceTutorRepository extends JpaRepository<AvanceTutor, Long> {
    List<AvanceTutor> findByInstanciaPracticaId(Long instanciaPracticaId);
    List<AvanceTutor> findByInstanciaPracticaIdAndCorte(Long instanciaPracticaId, Integer corte);
}
