package com.avh.practicas.estudiante.dto;

import com.avh.practicas.estudiante.entity.Estudiante;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultadoImportacion {
    private List<Estudiante> exitosos;
    private List<ErrorValidacion> errores;
    private int total;

    public double getTasaExito() {
        if (total == 0) {
            return 0.0;
        }
        return ((double) exitosos.size() / total) * 100.0;
    }
}
