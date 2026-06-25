package com.avh.practicas.shared.pattern.observer;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Payload inmutable de un evento emitido por un {@link Sujeto}.
 */
@Getter
public final class EventoSistema {

    private final String tipo;
    private final LocalDateTime fecha;
    private final Map<String, Object> datos;

    public EventoSistema(String tipo, LocalDateTime fecha, Map<String, Object> datos) {
        this.tipo = validarTipo(tipo);
        this.fecha = fecha != null ? fecha : LocalDateTime.now();
        this.datos = Collections.unmodifiableMap(
                datos != null ? new HashMap<>(datos) : new HashMap<>()
        );
    }

    public static EventoSistema crear(String tipo, Map<String, Object> datos) {
        return new EventoSistema(tipo, LocalDateTime.now(), datos);
    }

    public static EventoSistema crear(String tipo) {
        return crear(tipo, Map.of());
    }

    private String validarTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException("El tipo de evento es obligatorio");
        }
        return tipo.trim();
    }

    public Object obtenerDato(String clave) {
        return datos.get(clave);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventoSistema that)) {
            return false;
        }
        return Objects.equals(tipo, that.tipo)
                && Objects.equals(fecha, that.fecha)
                && Objects.equals(datos, that.datos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipo, fecha, datos);
    }

    @Override
    public String toString() {
        return "EventoSistema{tipo='" + tipo + "', fecha=" + fecha + ", datos=" + datos + '}';
    }
}
