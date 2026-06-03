package com.avh.practicas.calificacion.entity;

import com.avh.practicas.estudiante.entity.InstanciaPractica;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa la calificación final consolidada registrada por el Coordinador de Prácticas.
 */
@Entity
@Table(name = "notas_finales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotaFinal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instancia_practica_id", nullable = false, unique = true)
    private InstanciaPractica instanciaPractica;

    @Column(name = "nota_final", nullable = false)
    private Double notaFinal;

    @Column(nullable = false)
    private Boolean aprobada;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fecha = LocalDateTime.now();
}
