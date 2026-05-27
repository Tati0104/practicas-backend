package com.avh.practicas.estudiante.repository;

import com.avh.practicas.estudiante.entity.DocenteAsesor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocenteAsesorRepository extends JpaRepository<DocenteAsesor, Long> {

    @Query("select d from DocenteAsesor d join d.usuario u where u.correo = :correo")
    Optional<DocenteAsesor> findByCorreo(@Param("correo") String correo);

    @Query("select count(d) > 0 from DocenteAsesor d join d.usuario u where u.correo = :correo")
    boolean existsByCorreo(@Param("correo") String correo);

    List<DocenteAsesor> findByProgramaIdAndActivoTrue(Long programaId);
}
