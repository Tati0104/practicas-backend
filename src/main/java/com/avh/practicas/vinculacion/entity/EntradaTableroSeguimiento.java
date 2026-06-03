package com.avh.practicas.vinculacion.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bitacora_estudiante")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntradaTableroSeguimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "instancia_practica_id", nullable = false)
    private Long instanciaPracticaId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fecha = LocalDateTime.now();
}
