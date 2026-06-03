package com.avh.practicas.vinculacion.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "asignaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asignacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vacante_id", nullable = false)
    private Long vacanteId;

    @Column(name = "estudiante_id", nullable = false)
    private Long estudianteId;

    @Column(name = "instancia_practica_id")
    private Long instanciaPracticaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private EstadoAsignacion estado = EstadoAsignacion.ASIGNADA;

    @Column(name = "fecha_creacion", nullable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}
