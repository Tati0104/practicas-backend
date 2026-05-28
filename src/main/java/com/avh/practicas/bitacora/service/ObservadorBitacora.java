package com.avh.practicas.bitacora.service;

import com.avh.practicas.bitacora.entity.TipoAccion;
import com.avh.practicas.shared.evento.EventoSistema;
import com.avh.practicas.shared.evento.Observador;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ObservadorBitacora implements Observador {

    private final BitacoraService bitacoraService;

    @Override
    public void actualizar(EventoSistema evento) {
        bitacoraService.registrar(
                evento.getUsuarioId(),
                evento.getModulo(),
                convertirAccion(evento),
                evento.getIdRecurso(),
                null,
                evento.getDatos().toString()
        );
    }

    private TipoAccion convertirAccion(EventoSistema evento) {
        return switch (evento.getTipo()) {
            case VACANTE_APROBADA -> TipoAccion.APROBACION;
            case VACANTE_RECHAZADA -> TipoAccion.RECHAZO;
            case VACANTE_CERRADA -> TipoAccion.CIERRE;
            case DOCENTE_ASESOR_ACTIVADO -> TipoAccion.ACTIVACION;
            case DOCENTE_ASESOR_INACTIVADO -> TipoAccion.INACTIVACION;
            default -> TipoAccion.MODIFICACION;
        };
    }
}
