package com.avh.practicas.estudiante.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "instancias_practica")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstanciaPractica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expediente_id", nullable = false)
    private Expediente expediente;

    @Column(name = "numero_practica", nullable = false)
    private Integer numeroPractica;

    @Column(nullable = false)
    private String estado;
}
