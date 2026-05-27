package com.avh.practicas.empresa.entity;

import com.avh.practicas.configuracion.entity.CatalogoItem;
import com.avh.practicas.shared.pattern.observer.Observador;
import com.avh.practicas.shared.pattern.observer.Sujeto;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "empresas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Empresa implements Sujeto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nit;

    @Column(name = "razon_social", nullable = false)
    private String razonSocial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id", nullable = false)
    private CatalogoItem sector;

    @Column
    private String direccion;

    @Column
    private String municipio;

    @Column
    private String telefono;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

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
