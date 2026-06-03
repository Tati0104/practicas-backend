package com.avh.practicas.vinculacion.service;

import com.avh.practicas.bitacora.entity.TipoAccion;
import com.avh.practicas.bitacora.service.BitacoraService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServicioBitacoraImpl implements ServicioBitacora {

    private final BitacoraService bitacoraService;

    @Override
    @Transactional
    public void registrarVinculacionConfirmada(Long practicaId, Long usuarioId, String detalle) {
        bitacoraService.registrar(usuarioId, "VINCULACION", TipoAccion.MODIFICACION, practicaId, null, detalle);
    }
}
