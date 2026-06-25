package com.avh.practicas.vinculacion.repository;

import com.avh.practicas.vinculacion.entity.CategoriaDocumento;
import com.avh.practicas.vinculacion.entity.DocumentoPractica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentoPracticaRepository extends JpaRepository<DocumentoPractica, Long> {

    List<DocumentoPractica> findByInstanciaPracticaIdOrderByFechaDesc(Long instanciaPracticaId);

    List<DocumentoPractica> findByAsignacionIdOrderByFechaDesc(Long asignacionId);

    List<DocumentoPractica> findByInstanciaPracticaIdAndCategoriaOrderByFechaDesc(
            Long instanciaPracticaId,
            CategoriaDocumento categoria
    );
}
