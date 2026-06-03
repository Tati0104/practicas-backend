package com.avh.practicas.configuracion.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "programas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Programa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del programa no puede estar vacío")
    @Size(max = 150, message = "El nombre del programa no puede exceder los 150 caracteres")
    @Column(nullable = false, length = 150)
    private String nombre;

    @NotNull(message = "La facultad es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facultad_id", nullable = false)
    private Facultad facultad;

    @Column(name = "total_practicas", nullable = false)
    @Builder.Default
    private Integer totalPracticas = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}
