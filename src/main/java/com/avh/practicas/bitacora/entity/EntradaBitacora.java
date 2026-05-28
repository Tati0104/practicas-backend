package com.avh.practicas.bitacora.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bitacora_auditoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntradaBitacora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tabla_afectada", length = 100)
    private String tablaAfectada;

    @Column(name = "accion", length = 100)
    private String accion;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "detalle", columnDefinition = "TEXT")
    private String detalle;

    @Column(name = "fecha")
    private LocalDateTime fecha;
}