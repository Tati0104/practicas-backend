package com.avh.practicas.vinculacion.alerta.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "alertas_sistema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaSistema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private Boolean leida = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean resuelta = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean prioritaria = false;

    @Column(name = "url_accion")
    private String urlAccion;

    @Column(name = "nombre_modulo")
    private String nombreModulo;

    @Column(name = "condicion_resolucion")
    private String condicionResolucion;

    @Column(name = "fecha_archivado")
    private LocalDateTime fechaArchivado;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private TipoAlerta tipo;

    @Column(name = "instancia_practica_id")
    private Long instanciaPracticaId;
}
