package com.avh.practicas.vinculacion.observer;

import com.avh.practicas.bitacora.entity.TipoAccion;
import com.avh.practicas.bitacora.service.BitacoraService;
import com.avh.practicas.shared.pattern.observer.EventoSistema;
import com.avh.practicas.shared.pattern.observer.Observador;
import com.avh.practicas.vinculacion.event.EventoAsignacion;
import com.avh.practicas.vinculacion.support.UsuarioAutenticadoProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ObservadorAsignacionBitacora implements Observador {

    private final BitacoraService bitacoraService;
    private final UsuarioAutenticadoProvider usuarioProvider;

    @Override
    public void actualizar(EventoSistema evento) {
        actualizar(evento.getTipo(), evento.getDatos());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void actualizar(String evento, Object datos) {
        Map<String, Object> payload = datos instanceof Map ? (Map<String, Object>) datos : Map.of("detalle", String.valueOf(datos));
        Long asignacionId = longDato(payload, "asignacionId");
        Long usuarioId = usuarioProvider.obtener().getId();

        bitacoraService.registrar(
                usuarioId,
                "asignaciones",
                resolverAccion(evento),
                asignacionId,
                stringDato(payload, "estado_anterior"),
                construirDetalle(evento, payload)
        );
    }

    private TipoAccion resolverAccion(String evento) {
        return switch (evento) {
            case EventoAsignacion.NUEVA_ASIGNACION -> TipoAccion.CREACION;
            case EventoAsignacion.ASIGNACION_CANCELADA -> TipoAccion.CANCELACION;
            default -> TipoAccion.MODIFICACION;
        };
    }

    private String construirDetalle(String evento, Map<String, Object> datos) {
        return "Evento=" + evento
                + ", estado=" + stringDato(datos, "estado")
                + ", motivo=" + stringDato(datos, "motivo");
    }

    private Long longDato(Map<String, Object> datos, String clave) {
        Object valor = datos.get(clave);
        if (valor instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    private String stringDato(Map<String, Object> datos, String clave) {
        Object valor = datos.get(clave);
        return valor == null ? "" : String.valueOf(valor);
    }
}
