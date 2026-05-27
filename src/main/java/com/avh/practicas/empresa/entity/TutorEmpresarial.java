package com.avh.practicas.empresa.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tutores_empresariales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TutorEmpresarial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String cargo;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column(nullable = false)
    private String telefono;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}
