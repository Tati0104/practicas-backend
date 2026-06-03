package com.avh.practicas.seguimiento.entity;

import com.avh.practicas.estudiante.entity.DocenteAsesor;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa las observaciones académicas registradas por un Docente Asesor.
 */
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instancia_practica_id", nullable = false)
    private InstanciaPractica instanciaPractica;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docente_id")
    private DocenteAsesor docente;

    @Column(name = "corte")
    private Integer corte;

    @Column(name = "observacion", nullable = false)
    private String observacion;

    @Column(name = "visible_para_estudiante", nullable = false)
    @Builder.Default
    private Boolean visibleParaEstudiante = true;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fecha = LocalDateTime.now();
}
