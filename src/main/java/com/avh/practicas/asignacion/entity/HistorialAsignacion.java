package com.avh.practicas.asignacion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad de trazabilidad PE-31.
 * Cada registro guarda un cambio de estado con responsable, fecha y motivo.
 */
@Entity
@Table(name = "historial_asignaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialAsignacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asignacion_id", nullable = false)
    private Long asignacionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 50)
    private EstadoAsignacion estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false, length = 50)
    private EstadoAsignacion estadoNuevo;

    @Column(name = "responsable_id")
    private Long responsableId;

    @Column(columnDefinition = "TEXT")
    private String motivo;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @PrePersist
    public void prePersist() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }
}
