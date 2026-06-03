package com.avh.practicas.vinculacion.alerta;

import com.avh.practicas.vinculacion.alerta.dto.AlertaVista;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Aplica el criterio de {@link DecoradorPrioritario}: alertas prioritarias al inicio del listado.
 */
@Component
public class AlertaOrdenador {

    public List<Alerta> ordenar(List<Alerta> alertas) {
        return alertas.stream()
                .sorted(Comparator
                        .comparing((Alerta alerta) -> !alerta.mostrar().prioritaria())
                        .thenComparing(alerta -> alerta.mostrar().fecha(),
                                Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public List<AlertaVista> ordenarVistas(List<AlertaVista> vistas) {
        return vistas.stream()
                .sorted(Comparator
                        .comparing((AlertaVista vista) -> !vista.prioritaria())
                        .thenComparing(AlertaVista::fecha, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }
}
