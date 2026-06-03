package com.avh.practicas.vinculacion.repository;

import com.avh.practicas.vinculacion.entity.EntradaTableroSeguimiento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TableroSeguimientoRepository extends JpaRepository<EntradaTableroSeguimiento, Long> {
}
