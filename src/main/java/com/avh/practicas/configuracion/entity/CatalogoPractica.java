package com.avh.practicas.configuracion.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "catalogo_practicas", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"programa_id", "numero_practica"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogoPractica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El programa es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programa_id", nullable = false)
    private Programa programa;

    @NotNull(message = "El número de práctica es obligatorio")
    @Min(value = 1, message = "El número de práctica debe ser como mínimo 1")
    @Column(name = "numero_practica", nullable = false)
    private Integer numeroPractica;

    @NotBlank(message = "El nombre de la práctica no puede estar vacío")
    @Size(max = 150, message = "El nombre no puede exceder los 150 caracteres")
    @Column(nullable = false, length = 150)
    private String nombre;

    @NotBlank(message = "La materia núcleo no puede estar vacía")
    @Size(max = 150, message = "La materia núcleo no puede exceder los 150 caracteres")
    @Column(name = "materia_nucleo", nullable = false, length = 150)
    private String materiaNucleo;

    @NotBlank(message = "El código de la materia no puede estar vacío")
    @Size(max = 50, message = "El código de la materia no puede exceder los 50 caracteres")
    @Column(name = "codigo_materia", nullable = false, length = 50)
    private String codigoMateria;

    @NotNull(message = "El número de cortes es obligatorio")
    @Min(value = 1, message = "El número de cortes debe ser mayor o igual a 1")
    @Column(name = "num_cortes", nullable = false)
    private Integer numCortes;

    @NotNull(message = "La duración en semanas es obligatoria")
    @Min(value = 1, message = "La duración en semanas debe ser mayor o igual a 1")
    @Column(name = "duracion_semanas", nullable = false)
    private Integer duracionSemanas;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}
