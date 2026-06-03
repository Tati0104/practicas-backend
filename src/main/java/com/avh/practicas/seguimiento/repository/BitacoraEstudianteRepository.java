package com.avh.practicas.seguimiento.repository;

import com.avh.practicas.seguimiento.entity.BitacoraEstudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BitacoraEstudianteRepository extends JpaRepository<BitacoraEstudiante, Long> {
}
