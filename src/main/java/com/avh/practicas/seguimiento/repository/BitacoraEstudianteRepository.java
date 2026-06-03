package com.avh.practicas.seguimiento.repository;

import com.avh.practicas.seguimiento.entity.BitacoraEstudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad BitacoraEstudiante.
 */
@Repository
public interface BitacoraEstudianteRepository extends JpaRepository<BitacoraEstudiante, Long> {
    List<BitacoraEstudiante> findByInstanciaPracticaId(Long instanciaPracticaId);
    List<BitacoraEstudiante> findByInstanciaPracticaIdAndCorte(Long instanciaPracticaId, Integer corte);
    Optional<BitacoraEstudiante> findFirstByInstanciaPracticaIdOrderByFechaDesc(Long instanciaPracticaId);
}
