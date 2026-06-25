package com.avh.practicas.auth.dto;

import com.avh.practicas.shared.enums.Rol;
import com.avh.practicas.shared.enums.Scope;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioAdminResponse {

    private Long id;
    private String nombre;
    private String correo;
    private Rol rol;
    private Scope scope;
    private Boolean activo;
}