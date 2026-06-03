package com.avh.practicas.asignacion.repository;

import com.avh.practicas.asignacion.entity.HistorialAsignacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialAsignacionRepository extends JpaRepository<HistorialAsignacion, Long> {
    List<HistorialAsignacion> findByAsignacionIdOrderByFechaAsc(Long asignacionId);
}
