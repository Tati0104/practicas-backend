package com.avh.practicas.empresa.repository;

import com.avh.practicas.empresa.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long>, JpaSpecificationExecutor<Empresa> {
    boolean existsBySectorIdAndActivoTrue(Long sectorId);
    boolean existsByNit(String nit);
    Optional<Empresa> findByNit(String nit);

    @Query("SELECT DISTINCT e FROM Empresa e, Vacante v WHERE v.empresaId = e.id AND v.programaId = :programaId")
    List<Empresa> findByProgramaId(@Param("programaId") Long programaId);
}
