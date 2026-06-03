package com.avh.practicas.cierre.entity;

import com.avh.practicas.estudiante.entity.InstanciaPractica;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa la encuesta de satisfacción o evaluación para tutor y estudiante.
 */
@Entity
@Table(name = "encuestas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Encuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instancia_practica_id", nullable = false)
    private InstanciaPractica instanciaPractica;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEncuesta tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEncuesta estado;

    @Column(name = "fecha_envio_invitacion", nullable = false)
    @Builder.Default
    private LocalDateTime fechaEnvioInvitacion = LocalDateTime.now();

    @Column(name = "fecha_ultimo_recordatorio")
    private LocalDateTime fechaUltimoRecordatorio;

    @Column(name = "respuestas_json")
    private String respuestasJson;
}
