package com.avh.practicas.estudiante.entity;

import com.avh.practicas.configuracion.entity.Programa;
import com.avh.practicas.shared.pattern.observer.Observador;
import com.avh.practicas.shared.pattern.observer.Sujeto;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "estudiantes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Estudiante implements Sujeto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String identificacion;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column
    private String telefono;

    @Column(name = "contacto_emergencia")
    private String contactoEmergencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programa_id", nullable = false)
    private Programa programa;

    @Column
    private Integer semestre;

    @Column(name = "creditos_aprobados")
    private Integer creditosAprobados;

    @Column(name = "promedio_acumulado")
    private Double promedioAcumulado;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_aptitud", nullable = false)
    @Builder.Default
    private EstadoAptitud estadoAptitud = EstadoAptitud.SIN_EVALUAR;

    @OneToOne(mappedBy = "estudiante", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private Expediente expediente;

    @Transient
    @Builder.Default
    private List<Observador> observadores = new ArrayList<>();

    private List<Observador> getObservadoresSafe() {
        if (observadores == null) {
            observadores = new ArrayList<>();
        }
        return observadores;
    }

    public void registrarObservador(Observador observador) {
        if (observador != null && !getObservadoresSafe().contains(observador)) {
            getObservadoresSafe().add(observador);
        }
    }

    public void eliminarObservador(Observador observador) {
        getObservadoresSafe().remove(observador);
    }

    public void notificarObservadores(String evento, Object datos) {
        if (observadores != null) {
            for (Observador observador : observadores) {
                observador.actualizar(evento, datos);
            }
        }
    }
}