package com.avh.practicas.estudiante.repository;

import com.avh.practicas.estudiante.entity.EstadoPractica;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstanciaPracticaRepository extends JpaRepository<InstanciaPractica, Long> {
    boolean existsByExpedienteEstudianteProgramaIdAndEstadoIn(Long programaId, List<EstadoPractica> estados);
    boolean existsByExpedienteEstudianteProgramaIdAndNumeroPracticaAndEstadoIn(Long programaId, Integer numeroPractica, List<EstadoPractica> estados);
}
