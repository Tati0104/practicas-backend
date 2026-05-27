package com.avh.practicas.vacante.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "vacante")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vacante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vacante")
    private Long id;

    @Column(name = "id_empresa", nullable = false)
    private Long empresaId;

    @Column(name = "id_programa", nullable = false)
    private Long programaId;

    @Column(name = "id_creado_por")
    private Long creadoPorId;

    @Column(name = "id_aprobado_por")
    private Long aprobadoPorId;

    @Column(nullable = false, length = 200)
    private String cargo;

    @Column(name = "descripcion_perfil", columnDefinition = "TEXT")
    private String descripcionPerfil;

    @Column(columnDefinition = "TEXT")
    private String requisitos;

    @Column(name = "cupos_totales", nullable = false)
    private Integer cuposTotales;

    @Column(name = "cupos_ocupados", nullable = false)
    private Integer cuposOcupados;

    @Column(length = 100)
    private String area;

    @Column(length = 50)
    private String modalidad;

    @Column(name = "fecha_inicio_disponibilidad")
    private LocalDate fechaInicioDisponibilidad;

    @Column(name = "fecha_fin_disponibilidad")
    private LocalDate fechaFinDisponibilidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EstadoVacanteEnum estado;

    @Column(name = "motivo_rechazo", columnDefinition = "TEXT")
    private String motivoRechazo;

    public Integer getCuposDisponibles() {
        int totales = cuposTotales == null ? 0 : cuposTotales;
        int ocupados = cuposOcupados == null ? 0 : cuposOcupados;
        return Math.max(0, totales - ocupados);
    }
}
