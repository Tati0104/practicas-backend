package com.avh.practicas.usuario.repository;

import com.avh.practicas.usuario.entity.Rol;
import com.avh.practicas.usuario.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreo(String correo);

    List<Usuario> findByRolAndActivoTrue(Rol rol);

    boolean existsByCorreo(String correo);
}
