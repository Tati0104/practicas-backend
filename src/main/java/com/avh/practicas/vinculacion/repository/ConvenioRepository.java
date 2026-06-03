package com.avh.practicas.vinculacion.repository;

import com.avh.practicas.vinculacion.entity.Convenio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConvenioRepository extends JpaRepository<Convenio, Long> {

    Optional<Convenio> findByAsignacionId(Long asignacionId);

    Optional<Convenio> findByInstanciaPracticaId(Long instanciaPracticaId);
}
