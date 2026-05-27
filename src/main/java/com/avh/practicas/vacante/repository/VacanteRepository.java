package com.avh.practicas.vacante.repository;

import com.avh.practicas.vacante.entity.Vacante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VacanteRepository extends JpaRepository<Vacante, Long> {
    List<Vacante> findByProgramaId(Long programaId);
    List<Vacante> findByEmpresaId(Long empresaId);
    List<Vacante> findByEstadoDb(String estadoDb);
    boolean existsByEmpresaIdAndEstadoDb(Long empresaId, String estadoDb);
}
