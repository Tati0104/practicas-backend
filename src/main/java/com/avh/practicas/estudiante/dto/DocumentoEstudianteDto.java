package com.avh.practicas.estudiante.dto;

import com.avh.practicas.estudiante.entity.DocumentoEstudiante;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentoEstudianteDto {
    private Long id;
    private Long estudianteId;
    private String nombre;
    private String url;
    private String tipo;
    private LocalDateTime fecha;

    public static DocumentoEstudianteDto desde(DocumentoEstudiante entity) {
        return DocumentoEstudianteDto.builder()
                .id(entity.getId())
                .estudianteId(entity.getEstudianteId())
                .nombre(entity.getNombre())
                .url(entity.getUrl())
                .tipo(entity.getTipo())
                .fecha(entity.getFecha())
                .build();
    }
}
