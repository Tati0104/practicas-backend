package com.avh.practicas.shared.evento;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@Builder
public class EventoSistema {

    private final TipoEventoSistema tipo;
    private final LocalDateTime fecha;
    private final Long usuarioId;
    private final String modulo;
    private final Long idRecurso;
    private final Map<String, Object> datos;

    public static EventoSistema crear(TipoEventoSistema tipo, Long usuarioId, String modulo, Long idRecurso, Map<String, Object> datos) {
        return EventoSistema.builder()
                .tipo(tipo)
                .fecha(LocalDateTime.now())
                .usuarioId(usuarioId)
                .modulo(modulo)
                .idRecurso(idRecurso)
                .datos(datos == null ? new HashMap<>() : datos)
                .build();
    }
}
