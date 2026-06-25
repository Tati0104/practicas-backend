package com.avh.practicas.configuracion.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "facultades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Facultad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la facultad no puede estar vacío")
    @Size(max = 150, message = "El nombre de la facultad no puede exceder los 150 caracteres")
    @Column(nullable = false, unique = true, length = 150)
    private String nombre;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}
