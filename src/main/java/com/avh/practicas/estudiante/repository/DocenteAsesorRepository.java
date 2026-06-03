package com.avh.practicas.estudiante.repository;

import com.avh.practicas.estudiante.entity.DocenteAsesor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocenteAsesorRepository extends JpaRepository<DocenteAsesor, Long> {

    Optional<DocenteAsesor> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    List<DocenteAsesor> findByProgramaIdAndActivoTrue(Long programaId);

    List<DocenteAsesor> findByActivoTrue();
}
