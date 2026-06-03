package com.avh.practicas.asignacion.repository;

import com.avh.practicas.asignacion.entity.Asignacion;
import com.avh.practicas.asignacion.entity.EstadoAsignacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;

public interface AsignacionRepository extends JpaRepository<Asignacion, Long>, JpaSpecificationExecutor<Asignacion> {
    boolean existsByEstudianteIdAndEstadoIn(Long estudianteId, Collection<EstadoAsignacion> estados);
    List<Asignacion> findByEstudianteId(Long estudianteId);
    List<Asignacion> findByVacanteId(Long vacanteId);
}
