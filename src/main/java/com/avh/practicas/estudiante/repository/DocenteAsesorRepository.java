package com.avh.practicas.estudiante.repository;

import com.avh.practicas.estudiante.entity.DocenteAsesor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocenteAsesorRepository extends JpaRepository<DocenteAsesor, Long> {

    Optional<DocenteAsesor> findByCorreo(String correo);

    @Query("SELECT d FROM DocenteAsesor d WHERE LOWER(d.correo) = LOWER(:correo)")
    Optional<DocenteAsesor> findByCorreoIgnoreCase(@Param("correo") String correo);

    Optional<DocenteAsesor> findByUsuario_Id(Long usuarioId);

    boolean existsByCorreo(String correo);

    List<DocenteAsesor> findByProgramaIdAndActivoTrue(Long programaId);

    List<DocenteAsesor> findByActivoTrue();
}
