package com.avh.practicas.estudiante.entity;

import com.avh.practicas.usuario.entity.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "docente_asesor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocenteAsesor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_docente_asesor")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "id_programa", nullable = false)
    private Long programaId;

    @Column(name = "area_conocimiento", length = 150)
    private String areaConocimiento;

    @Column(nullable = false)
    private Boolean activo;

    public String getNombreCompleto() {
        return usuario == null ? null : usuario.getNombreCompleto();
    }

    public String getCorreo() {
        return usuario == null ? null : usuario.getCorreo();
    }
}
