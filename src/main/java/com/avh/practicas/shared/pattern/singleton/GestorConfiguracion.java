package com.avh.practicas.shared.pattern.singleton;

public class GestorConfiguracion {

    private static volatile GestorConfiguracion instancia;

    private int creditosMinimos = 80; // Valor por defecto
    private double promedioMinimo = 3.5; // Valor por defecto

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

    public int getCreditosMinimos() {
        return creditosMinimos;
    }

    public void setCreditosMinimos(int creditosMinimos) {
        this.creditosMinimos = creditosMinimos;
    }

    public double getPromedioMinimo() {
        return promedioMinimo;
    }

    public void setPromedioMinimo(double promedioMinimo) {
        this.promedioMinimo = promedioMinimo;
    }
}
