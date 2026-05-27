package com.avh.practicas.correo.repository;

import com.avh.practicas.correo.entity.PlantillaCorreo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlantillaCorreoRepository extends JpaRepository<PlantillaCorreo, Long> {

    Optional<PlantillaCorreo> findByCodigo(String codigo);
}
