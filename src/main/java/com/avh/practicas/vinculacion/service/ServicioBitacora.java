package com.avh.practicas.vinculacion.service;

public interface ServicioBitacora {

    void registrarVinculacionConfirmada(Long practicaId, Long usuarioId, String detalle);
}
