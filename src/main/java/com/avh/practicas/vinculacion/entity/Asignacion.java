package com.avh.practicas.vinculacion.entity;

import com.avh.practicas.shared.pattern.observer.EventoSistema;
import com.avh.practicas.shared.pattern.observer.Observador;
import com.avh.practicas.shared.pattern.observer.Sujeto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "asignaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asignacion implements Sujeto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vacante_id", nullable = false)
    private Long vacanteId;

    @Column(name = "estudiante_id", nullable = false)
    private Long estudianteId;

    @Column(name = "instancia_practica_id")
    private Long instanciaPracticaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private EstadoAsignacion estado = EstadoAsignacion.ASIGNADA;

    @Column(name = "fecha_creacion", nullable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "motivo_cancelacion", columnDefinition = "TEXT")
    private String motivoCancelacion;

    @Transient
    @Builder.Default
    private List<Observador> observadores = new ArrayList<>();

    private List<Observador> getObservadoresSafe() {
        if (observadores == null) {
            observadores = new ArrayList<>();
        }
        return observadores;
    }

    @Override
    public void agregarObservador(Observador observador) {
        registrarObservador(observador);
    }

    @Override
    public void eliminarObservador(Observador observador) {
        getObservadoresSafe().remove(observador);
    }

    public void registrarObservador(Observador observador) {
        if (observador != null && !getObservadoresSafe().contains(observador)) {
            getObservadoresSafe().add(observador);
        }
    }

    @Override
    public void notificar(EventoSistema evento) {
        notificarObservadores(evento.getTipo(), evento.getDatos());
    }

    @Override
    public void notificarObservadores(String evento, Object datos) {
        if (observadores == null || observadores.isEmpty()) {
            return;
        }
        for (Observador observador : observadores) {
            if (datos instanceof java.util.Map<?, ?> mapa) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> datosEvento = (java.util.Map<String, Object>) mapa;
                observador.actualizar(evento, datosEvento);
            } else {
                observador.actualizar(evento, datos);
            }
        }
    }
}
