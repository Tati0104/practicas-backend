package com.avh.practicas.configuracion.repository;

import com.avh.practicas.configuracion.entity.CatalogoPractica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CatalogoPracticaRepository extends JpaRepository<CatalogoPractica, Long> {
    List<CatalogoPractica> findByProgramaId(Long programaId);
    List<CatalogoPractica> findByProgramaIdAndActivoTrue(Long programaId);
    Optional<CatalogoPractica> findByProgramaIdAndNumeroPractica(Long programaId, Integer numeroPractica);
    boolean existsByProgramaIdAndNumeroPractica(Long programaId, Integer numeroPractica);
    boolean existsByProgramaIdAndNumeroPracticaAndIdNot(Long programaId, Integer numeroPractica, Long id);
}
