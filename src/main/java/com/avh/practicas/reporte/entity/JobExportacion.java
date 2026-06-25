package com.avh.practicas.reporte.entity;

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

@Entity
@Table(name = "jobs_exportacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobExportacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_reporte", nullable = false, length = 100)
    private String tipoReporte;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EstadoJobExportacion estado;

    @Column(name = "url_archivo")
    private String urlArchivo;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "mensaje", columnDefinition = "TEXT")
    private String mensaje;

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
}
