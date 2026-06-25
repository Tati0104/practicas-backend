package com.avh.practicas.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDto {
    private Long totalEstudiantes;
    private Long totalEmpresas;
    private Long totalVacantes;

    private Long usuariosActivos;
    private Long estudiantesEnPractica;
    private Long empresasActivas;
    private Long vacantesActivas;
    private Long vacantesParaAprobar;
    private Long asignacionesActivas;
    private Long cierresPendientes;
    private Long estudiantesSinEvaluar;
    private Long aptosSinIniciar;
    private Long estudiantesAsignados;
    private Long calificacionesPendientes;
    private Long firmasPendientes;
}
