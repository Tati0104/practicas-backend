package com.avh.practicas.seguimiento.entity;

import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa los avances de seguimiento registrados por un Tutor Empresarial.
 */
@Entity
@Table(name = "avances_tutor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvanceTutor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instancia_practica_id", nullable = false)
    private InstanciaPractica instanciaPractica;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id")
    private TutorEmpresarial tutor;

    @Column(name = "corte")
    private Integer corte;

    @Column(name = "avance", nullable = false)
    private String avance;

    @Column(name = "logros")
    private String logros;

    @Column(name = "dificultades")
    private String dificultades;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fecha = LocalDateTime.now();
}
