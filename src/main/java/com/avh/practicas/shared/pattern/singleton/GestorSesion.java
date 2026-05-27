package com.avh.practicas.shared.pattern.singleton;

import java.util.HashMap;
import java.util.Map;

public class GestorSesion {

    private static final String ROL_INVITADO = "INVITADO";
    private static final String SCOPE_PUBLICO = "PUBLICO";

    private static final Map<String, Integer> JERARQUIA_ROLES = crearJerarquiaRoles();

    private static volatile GestorSesion instancia;

    private String usuarioActual;
    private String rolActual;
    private String scopeActual;

    private GestorSesion() {
        cerrarSesion();
    }

    public static GestorSesion getInstancia() {
        if (instancia == null) {
            synchronized (GestorSesion.class) {
                if (instancia == null) {
                    instancia = new GestorSesion();
                }
            }
        }
        return instancia;
    }

    public synchronized void iniciarSesion(String usuario) {
        validarTextoObligatorio(usuario, "El usuario es obligatorio");
        this.usuarioActual = usuario.trim();
        this.rolActual = "USER";
        this.scopeActual = "APP";
    }

    public synchronized void iniciarSesion(String usuario, String rol, String scope) {
        validarTextoObligatorio(usuario, "El usuario es obligatorio");
        validarTextoObligatorio(rol, "El rol es obligatorio");
        validarTextoObligatorio(scope, "El scope es obligatorio");
        this.usuarioActual = usuario.trim();
        this.rolActual = rol.trim().toUpperCase();
        this.scopeActual = scope.trim().toUpperCase();
    }

    public synchronized void cerrarSesion() {
        this.usuarioActual = null;
        this.rolActual = ROL_INVITADO;
        this.scopeActual = SCOPE_PUBLICO;
    }

    public synchronized String getRol() {
        return rolActual;
    }

    public synchronized String getScope() {
        return scopeActual;
    }

    public synchronized boolean tienePermiso(String modulo, String accion) {
        validarTextoObligatorio(modulo, "El modulo es obligatorio");
        validarTextoObligatorio(accion, "La accion es obligatoria");

        if ("PUBLICO".equals(scopeActual)) {
            return "LECTURA".equals(normalizar(accion));
        }

        int nivelActual = JERARQUIA_ROLES.getOrDefault(rolActual, 0);
        int nivelRequerido = resolverNivelRequerido(normalizar(accion));

        return nivelActual >= nivelRequerido;
    }

    private static Map<String, Integer> crearJerarquiaRoles() {
        Map<String, Integer> jerarquia = new HashMap<>();
        jerarquia.put("INVITADO", 0);
        jerarquia.put("USER", 1);
        jerarquia.put("EDITOR", 2);
        jerarquia.put("ADMIN", 3);
        return Map.copyOf(jerarquia);
    }

    private int resolverNivelRequerido(String accionNormalizada) {
        return switch (accionNormalizada) {
            case "LECTURA", "CONSULTA", "VER" -> 0;
            case "CREAR", "REGISTRAR" -> 1;
            case "EDITAR", "ACTUALIZAR" -> 2;
            case "ELIMINAR", "BORRAR", "ADMINISTRAR" -> 3;
            default -> 3;
        };
    }

    private String normalizar(String texto) {
        return texto == null ? "" : texto.trim().toUpperCase();
    }

    private void validarTextoObligatorio(String texto, String mensajeError) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(mensajeError);
        }
    }
}
