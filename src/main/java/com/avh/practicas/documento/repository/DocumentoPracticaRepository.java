package com.avh.practicas.documento.repository;

import com.avh.practicas.documento.entity.DocumentoPractica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentoPracticaRepository extends JpaRepository<DocumentoPractica, Long> {

    @Query("""
            SELECT d FROM DocumentoPractica d
            LEFT JOIN FETCH d.usuarioCarga
            WHERE d.instanciaPractica.id = :instanciaPracticaId
            ORDER BY d.fechaCarga DESC
            """)
    List<DocumentoPractica> findByInstanciaPracticaIdOrderByFechaCargaDesc(
            @Param("instanciaPracticaId") Long instanciaPracticaId);

    @Query("""
            SELECT d FROM DocumentoPractica d
            JOIN FETCH d.instanciaPractica ip
            JOIN FETCH ip.expediente e
            JOIN FETCH e.estudiante
            WHERE d.id = :id AND ip.id = :instanciaPracticaId
            """)
    Optional<DocumentoPractica> findByIdAndInstanciaPracticaId(
            @Param("id") Long id,
            @Param("instanciaPracticaId") Long instanciaPracticaId);
}
