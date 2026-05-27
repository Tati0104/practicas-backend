package com.avh.practicas.shared.pattern.singleton;

import java.util.Objects;

public class ConfigPrograma {

    private final int maxLom;
    private final int configPrograma;

    public ConfigPrograma(int maxLom, int configPrograma) {
        this.maxLom = validarNoNegativo(maxLom, "maxLom");
        this.configPrograma = validarNoNegativo(configPrograma, "configPrograma");
    }

    public int getMaxLom() {
        return maxLom;
    }

    public int getConfigPrograma() {
        return configPrograma;
    }

    private int validarNoNegativo(int valor, String nombreCampo) {
        if (valor < 0) {
            throw new IllegalArgumentException(nombreCampo + " no puede ser negativo");
        }
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConfigPrograma that)) {
            return false;
        }
        return maxLom == that.maxLom && configPrograma == that.configPrograma;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxLom, configPrograma);
    }

    @Override
    public String toString() {
        return "ConfigPrograma{" +
                "maxLom=" + maxLom +
                ", configPrograma=" + configPrograma +
                '}';
    }
}
