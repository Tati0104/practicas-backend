package com.avh.practicas.vinculacion.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "convenios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Convenio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "asignacion_id")
    private Long asignacionId;

    @Column(name = "instancia_practica_id")
    private Long instanciaPracticaId;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String estado = "ACTIVO";

    @Column(name = "url_documento")
    private String urlDocumento;

    @Column(name = "firma_coordinador_at")
    private LocalDateTime firmaCoordinadorAt;

    @Column(name = "firma_tutor_at")
    private LocalDateTime firmaTutorAt;

    @Column(name = "firma_estudiante_at")
    private LocalDateTime firmaEstudianteAt;

    public boolean tieneTresFirmas() {
        return firmaCoordinadorAt != null && firmaTutorAt != null && firmaEstudianteAt != null;
    }
}
