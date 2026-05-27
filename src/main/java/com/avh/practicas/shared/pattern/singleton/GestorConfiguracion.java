package com.avh.practicas.shared.pattern.singleton;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GestorConfiguracion {

    private static volatile GestorConfiguracion instancia;

    private final Map<String, ConfigPrograma> configuracionesPorPrograma;

    private GestorConfiguracion() {
        this.configuracionesPorPrograma = new HashMap<>();
    }

    public static GestorConfiguracion getInstancia() {
        if (instancia == null) {
            synchronized (GestorConfiguracion.class) {
                if (instancia == null) {
                    instancia = new GestorConfiguracion();
                }
            }
        }
        return instancia;
    }

    public synchronized void setConfig(String programa, ConfigPrograma config) {
        validarPrograma(programa);
        Objects.requireNonNull(config, "La configuracion no puede ser nula");
        configuracionesPorPrograma.put(programa, config);
    }

    public synchronized ConfigPrograma getConfig(String programa) {
        validarPrograma(programa);
        return configuracionesPorPrograma.get(programa);
    }

    public synchronized void recargarParametros(String programa, int maxLom, int configPrograma) {
        validarPrograma(programa);
        configuracionesPorPrograma.put(programa, new ConfigPrograma(maxLom, configPrograma));
    }

    public synchronized Map<String, ConfigPrograma> obtenerConfiguraciones() {
        return Collections.unmodifiableMap(new HashMap<>(configuracionesPorPrograma));
    }

    private void validarPrograma(String programa) {
        if (programa == null || programa.isBlank()) {
            throw new IllegalArgumentException("El nombre del programa es obligatorio");
        }
    }
}
