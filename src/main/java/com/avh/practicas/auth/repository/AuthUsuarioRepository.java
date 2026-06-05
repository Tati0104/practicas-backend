package com.avh.practicas.auth.repository;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.shared.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface AuthUsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {

    Optional<Usuario> findByCorreo(String correo);

    Optional<Usuario> findByTokenRecuperacion(String tokenRecuperacion);

    List<Usuario> findByRolAndActivo(Rol rol, Boolean activo);

    boolean existsByCorreo(String correo);

    long countByRolAndActivo(Rol rol, Boolean activo);
}