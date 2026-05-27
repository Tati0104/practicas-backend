package com.avh.practicas.empresa.repository;

import com.avh.practicas.empresa.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    boolean existsBySectorIdAndActivoTrue(Long sectorId);
}
