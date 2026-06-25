package com.avh.practicas.estudiante.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expediente_id", nullable = false)
    private Expediente expediente;

    @Column(name = "numero_practica", nullable = false)
    private Integer numeroPractica;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(name = "materia_nucleo", nullable = false, length = 150)
    private String materiaNucleo;

    @Column(name = "codigo_materia", nullable = false, length = 50)
    private String codigoMateria;

    @Column(name = "num_cortes", nullable = false)
    private Integer numCortes;

    @Column(name = "duracion_semanas", nullable = false)
    private Integer duracionSemanas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPractica estado;

    @Column(name = "empresa_id")
    private Long empresaId;

    @Column(name = "docente_asesor_id")
    private Long docenteAsesorId;

    @Column(name = "tutor_id")
    private Long tutorId;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(nullable = false)
    @Builder.Default
    private Boolean inmutable = false;
}
