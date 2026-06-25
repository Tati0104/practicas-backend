package com.avh.practicas.seguimiento.service;

import com.avh.practicas.auth.entity.Usuario;

public interface BitacoraService {
    void registrar(String tablaAfectada, String accion, Usuario usuario, String detalle);
}
