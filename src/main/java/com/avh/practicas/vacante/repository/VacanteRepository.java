package com.avh.practicas.vacante.repository;

import com.avh.practicas.vacante.entity.EstadoVacanteEnum;
import com.avh.practicas.vacante.entity.Vacante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface VacanteRepository extends JpaRepository<Vacante, Long>, JpaSpecificationExecutor<Vacante> {

    boolean existsByEmpresaIdAndEstado(Long empresaId, EstadoVacanteEnum estado);
    List<Vacante> findByEmpresaId(Long empresaId);

    default boolean existsByEmpresaIdAndEstado(Long empresaId, String estado) {
        return existsByEmpresaIdAndEstado(empresaId, EstadoVacanteEnum.valueOf(estado));
    }
}
