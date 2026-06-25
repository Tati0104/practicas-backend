package com.avh.practicas.bitacora.repository;

import com.avh.practicas.bitacora.entity.EntradaBitacora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EntradaBitacoraRepository extends JpaRepository<EntradaBitacora, Long>, JpaSpecificationExecutor<EntradaBitacora> {
}
