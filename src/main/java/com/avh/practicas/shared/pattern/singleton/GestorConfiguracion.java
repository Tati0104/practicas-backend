package com.avh.practicas.shared.pattern.singleton;

public class GestorConfiguracion {

    private static volatile GestorConfiguracion instancia;

    private int    creditosMinimos = 80;
    private double promedioMinimo  = 3.5;
    private double maxNota         = 5.0;
    private final ConfigSistema config = new ConfigSistema();

    private GestorConfiguracion() {
        // Constructor privado para evitar instanciación externa
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

    public int getCreditosMinimos() { return creditosMinimos; }
    public void setCreditosMinimos(int creditosMinimos) {
        this.creditosMinimos = creditosMinimos;
    }

    public double getPromedioMinimo() { return promedioMinimo; }
    public void setPromedioMinimo(double promedioMinimo) {
        this.promedioMinimo = promedioMinimo;
    }

    /**
     * Nota mínima de aprobación para cierre de práctica (PE-43 — Singleton).
     */
    public double getNotaMinimaAprobacion() {
        return promedioMinimo;
    }

    public double getMaxNota() { return maxNota; }
    public void setMaxNota(double maxNota) {
        this.maxNota = maxNota;
    }

    public ConfigSistema getConfig() { return config; }

    /**
     * Configuración global del sistema (PE-37 — umbral de inactividad).
     */
    public static class ConfigSistema {
        private int umbralInactividadDias = 7;

        public int getUmbralInactividadDias() { return umbralInactividadDias; }
        public void setUmbralInactividadDias(int dias) {
            this.umbralInactividadDias = dias;
        }
    }
}