package com.avh.practicas.vacante.entity;

import com.avh.practicas.configuracion.entity.CatalogoItem;
import com.avh.practicas.configuracion.entity.Programa;
import com.avh.practicas.empresa.entity.Empresa;
import com.avh.practicas.shared.pattern.observer.Observador;
import com.avh.practicas.shared.pattern.observer.Sujeto;
import com.avh.practicas.vacante.state.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vacantes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vacante implements Sujeto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programa_id", nullable = false)
    private Programa programa;

    @Column(nullable = false)
    private String cargo;

    @Column(nullable = false, length = 1000)
    private String descripcion;

    @Column(name = "perfil_requisitos", nullable = false, length = 1000)
    private String perfilRequisitos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modalidad_id", nullable = false)
    private CatalogoItem modalidad;

    @Column(name = "cupos_total", nullable = false)
    private Integer cuposTotal;

    @Column(name = "cupos_disponibles", nullable = false)
    private Integer cuposDisponibles;

    @Column(name = "estado", nullable = false)
    @Builder.Default
    private String estadoDb = "DISPONIBLE";

    @Column(name = "fecha_disponibilidad_inicio", nullable = false)
    private LocalDate fechaDisponibilidadInicio;

    @Column(name = "fecha_disponibilidad_fin", nullable = false)
    private LocalDate fechaDisponibilidadFin;

    @Transient
    private EstadoVacante estadoActual;

    @Transient
    @Builder.Default
    private List<Observador> observadores = new ArrayList<>();

    public EstadoVacante getEstadoActual() {
        if (estadoActual == null) {
            switch (estadoDb != null ? estadoDb.toUpperCase() : "DISPONIBLE") {
                case "ASIGNADA":
                    estadoActual = new EstadoAsignada();
                    break;
                case "CERRADA":
                    estadoActual = new EstadoCerrada();
                    break;
                case "DISPONIBLE":
                default:
                    estadoActual = new EstadoDisponible();
                    break;
            }
        }
        return estadoActual;
    }

    public void setEstadoActual(EstadoVacante estado) {
        this.estadoActual = estado;
        this.estadoDb = estado.getNombre();
    }

    @PostLoad
    private void postLoad() {
        getEstadoActual();
    }

    @PrePersist
    @PreUpdate
    private void prePersist() {
        if (estadoActual != null) {
            this.estadoDb = estadoActual.getNombre();
        }
    }

    // Métodos del patrón State
    public void publicar() {
        getEstadoActual().publicar(this);
    }

    public void asignar() {
        getEstadoActual().asignar(this);
    }

    public void cerrar() {
        getEstadoActual().cerrar(this);
    }

    // Métodos del patrón Observer
    private List<Observador> getObservadoresSafe() {
        if (observadores == null) {
            observadores = new ArrayList<>();
        }
        return observadores;
    }

    @Override
    public void registrarObservador(Observador observador) {
        if (observador != null && !getObservadoresSafe().contains(observador)) {
            getObservadoresSafe().add(observador);
        }
    }

    @Override
    public void eliminarObservador(Observador observador) {
        getObservadoresSafe().remove(observador);
    }

    @Override
    public void notificarObservadores(String evento, Object datos) {
        if (observadores != null) {
            for (Observador observador : observadores) {
                observador.actualizar(evento, datos);
            }
        }
    }
}
