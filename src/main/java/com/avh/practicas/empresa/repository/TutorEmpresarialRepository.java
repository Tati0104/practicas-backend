package com.avh.practicas.empresa.repository;

import com.avh.practicas.empresa.entity.TutorEmpresarial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TutorEmpresarialRepository extends JpaRepository<TutorEmpresarial, Long> {
    List<TutorEmpresarial> findByEmpresaId(Long empresaId);
    List<TutorEmpresarial> findByEmpresaIdAndActivoTrue(Long empresaId);
    boolean existsByCorreo(String correo);
    boolean existsByCorreoAndIdNot(String correo, Long id);
    Optional<TutorEmpresarial> findByCorreo(String correo);

    @Query("SELECT t FROM TutorEmpresarial t WHERE LOWER(t.correo) = LOWER(:correo)")
    Optional<TutorEmpresarial> findByCorreoIgnoreCase(@Param("correo") String correo);

    Optional<TutorEmpresarial> findByUsuarioId(Long usuarioId);
}
