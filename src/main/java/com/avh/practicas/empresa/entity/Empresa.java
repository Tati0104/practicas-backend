package com.avh.practicas.empresa.entity;

import com.avh.practicas.configuracion.entity.CatalogoItem;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "empresas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Empresa {

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

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}
