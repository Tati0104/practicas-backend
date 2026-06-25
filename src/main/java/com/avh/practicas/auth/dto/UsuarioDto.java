package com.avh.practicas.auth.dto;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.shared.enums.Rol;
import com.avh.practicas.shared.enums.Scope;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioDto {

    private Long id;
    private String nombre;
    private String correo;
    private Rol rol;
    private Scope scope;
    private Boolean activo;
    private Boolean primeraVez;

    public static UsuarioDto fromEntity(Usuario usuario) {
        return UsuarioDto.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .correo(usuario.getCorreo())
                .rol(usuario.getRol())
                .scope(usuario.getScope())
                .activo(usuario.getActivo())
                .primeraVez(usuario.getPrimeraVez())
                .build();
    }
}
