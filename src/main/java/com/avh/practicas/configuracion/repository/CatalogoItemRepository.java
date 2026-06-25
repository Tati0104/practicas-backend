package com.avh.practicas.configuracion.repository;

import com.avh.practicas.configuracion.entity.CatalogoItem;
import com.avh.practicas.configuracion.entity.TipoCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CatalogoItemRepository extends JpaRepository<CatalogoItem, Long> {
    List<CatalogoItem> findByTipo(TipoCatalogo tipo);
    List<CatalogoItem> findByTipoAndActivoTrue(TipoCatalogo tipo);
    boolean existsByTipoAndNombreIgnoreCase(TipoCatalogo tipo, String nombre);
    boolean existsByTipoAndNombreIgnoreCaseAndIdNot(TipoCatalogo tipo, String nombre, Long id);
}
