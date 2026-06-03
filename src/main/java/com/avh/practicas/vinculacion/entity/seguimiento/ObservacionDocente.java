package com.avh.practicas.vinculacion.entity.seguimiento;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "observaciones_docente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObservacionDocente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "instancia_practica_id", nullable = false)
    private Long instanciaPracticaId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String observacion;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fecha = LocalDateTime.now();
}
