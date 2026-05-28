package com.avh.practicas.vacante.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "vacantes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vacante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "programa_id", nullable = false)
    private Long programaId;

    @Column(name = "creado_por_id")
    private Long creadoPorId;

    @Column(name = "aprobado_por_id")
    private Long aprobadoPorId;

    @Column(nullable = false, length = 150)
    private String cargo;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcionPerfil;

    @Column(name = "perfil_requisitos", columnDefinition = "TEXT")
    private String requisitos;

    @Column(nullable = false, length = 50)
    private String modalidad;

    @Column(length = 150)
    private String area;

    @Column(name = "cupos_total", nullable = false)
    private Integer cuposTotales;

    @Column(name = "cupos_disponibles", nullable = false)
    private Integer cuposDisponibles;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EstadoVacanteEnum estado;

    @Column(name = "motivo_rechazo", columnDefinition = "TEXT")
    private String motivoRechazo;

    @Column(name = "fecha_disponibilidad_inicio")
    private LocalDate fechaInicioDisponibilidad;

    @Column(name = "fecha_disponibilidad_fin")
    private LocalDate fechaFinDisponibilidad;

    @Transient
    public Integer getCuposOcupados() {
        if (cuposTotales == null || cuposDisponibles == null) {
            return 0;
        }
        return Math.max(cuposTotales - cuposDisponibles, 0);
    }
}
