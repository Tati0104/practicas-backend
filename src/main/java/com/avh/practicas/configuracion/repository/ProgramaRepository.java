package com.avh.practicas.configuracion.repository;

import com.avh.practicas.configuracion.entity.Programa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgramaRepository extends JpaRepository<Programa, Long> {
    List<Programa> findByFacultadId(Long facultadId);
    List<Programa> findByFacultadIdAndActivoTrue(Long facultadId);
    boolean existsByFacultadIdAndActivoTrue(Long facultadId);
    boolean existsByNombreIgnoreCaseAndFacultadId(String nombre, Long facultadId);
    boolean existsByNombreIgnoreCaseAndFacultadIdAndIdNot(String nombre, Long facultadId, Long id);
}
