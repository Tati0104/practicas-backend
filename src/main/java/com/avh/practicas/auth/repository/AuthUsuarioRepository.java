package com.avh.practicas.auth.repository;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.shared.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AuthUsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {

    Optional<Usuario> findByCorreo(String correo);

    @Query("SELECT u FROM Usuario u WHERE LOWER(u.correo) = LOWER(:correo)")
    Optional<Usuario> findByCorreoIgnoreCase(@Param("correo") String correo);

    Optional<Usuario> findByTokenRecuperacion(String tokenRecuperacion);

    List<Usuario> findByRolAndActivo(Rol rol, Boolean activo);

    boolean existsByCorreo(String correo);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM Usuario u WHERE LOWER(u.correo) = LOWER(:correo)")
    boolean existsByCorreoIgnoreCase(@Param("correo") String correo);

    @Query("""
            SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END
            FROM Usuario u
            WHERE LOWER(u.correo) = LOWER(:correo)
              AND u.id <> :id
            """)
    boolean existsByCorreoIgnoreCaseAndIdNot(@Param("correo") String correo, @Param("id") Long id);

    long countByRolAndActivo(Rol rol, Boolean activo);

    long countByActivoTrue();
}