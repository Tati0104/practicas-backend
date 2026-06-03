package com.avh.practicas.vinculacion.alerta.repository;

import com.avh.practicas.vinculacion.alerta.entity.AlertaSistema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertaSistemaRepository extends JpaRepository<AlertaSistema, Long> {

    List<AlertaSistema> findByResueltaFalseOrderByFechaDesc();
}
