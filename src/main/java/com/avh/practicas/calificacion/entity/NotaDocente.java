package com.avh.practicas.calificacion.entity;

import com.avh.practicas.estudiante.entity.InstanciaPractica;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa la calificación registrada por un Docente Asesor para un corte.
 */
@Entity
@Table(name = "notas_docente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotaDocente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instancia_practica_id", nullable = false)
    private InstanciaPractica instanciaPractica;

    @Column(nullable = false)
    private Double nota;

    @Column(nullable = false)
    private Integer corte;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fecha = LocalDateTime.now();

    @Column
    private String observaciones;
}
