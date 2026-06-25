package com.avh.practicas.vinculacion.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "documentos_practica")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentoPractica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "instancia_practica_id", nullable = false)
    private Long instanciaPracticaId;

    @Column(name = "asignacion_id")
    private Long asignacionId;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false, length = 100)
    private String tipo;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private CategoriaDocumento categoria;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fecha = LocalDateTime.now();
}
