package com.avh.practicas.usuario.dto;

import com.avh.practicas.shared.enums.Rol;
import com.avh.practicas.shared.enums.Scope;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UsuarioDto {
    private Long   id;
    private String nombre;
    private String correo;
    private Rol    rol;
    private Scope  scope;
    private boolean activo;
    private boolean primeraVez;
    private Long   programaId;
    private Long   facultadId;
    private Long   empresaId;
    private String cargoTutor;
    private String telefonoTutor;
    private String identificacion;
    private String telefono;
}