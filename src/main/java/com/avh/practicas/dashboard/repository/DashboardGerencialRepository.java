package com.avh.practicas.dashboard.repository;

import com.avh.practicas.dashboard.support.PeriodoAcademicoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Consultas agregadas del dashboard gerencial (PE-45).
 */
@Repository
@RequiredArgsConstructor
public class DashboardGerencialRepository {

    private static final String JOIN_BASE = """
            FROM instancias_practica ip
            JOIN expedientes exp ON exp.id = ip.expediente_id
            JOIN estudiantes est ON est.id = exp.estudiante_id
            JOIN programas prog ON prog.id = est.programa_id
            JOIN facultades fac ON fac.id = prog.facultad_id
            """;

    private final JdbcTemplate jdbcTemplate;

    public Map<String, Integer> totalPracticantesActivosPorFacultad(
            Optional<PeriodoAcademicoUtil.PeriodoAcademico> periodo,
            Long facultadId) {

        FiltroSql filtro = filtroPracticaActiva(periodo, facultadId);
        String sql = """
                SELECT fac.nombre, COUNT(DISTINCT est.id) AS total
                """
                + JOIN_BASE
                + """
                WHERE ip.estado = 'EN_CURSO'
                """
                + filtro.clausula()
                + """
                 GROUP BY fac.id, fac.nombre
                 ORDER BY fac.nombre
                """;

        Map<String, Integer> resultado = new LinkedHashMap<>();
        jdbcTemplate.query(sql, filtro.parametros(), rs -> {
            resultado.put(rs.getString("nombre"), rs.getInt("total"));
        });
        return resultado;
    }

    public double tasaAprobacionGlobal(
            Optional<PeriodoAcademicoUtil.PeriodoAcademico> periodo,
            Long facultadId) {

        FiltroSql filtro = filtroPracticasCerradas(periodo, facultadId);
        String sql = """
                SELECT
                    COUNT(*) FILTER (WHERE ip.estado = 'COMPLETADA') AS aprobadas,
                    COUNT(*) FILTER (WHERE ip.estado IN ('COMPLETADA', 'REPROBADA')) AS cerradas
                """
                + JOIN_BASE
                + """
                WHERE ip.estado IN ('COMPLETADA', 'REPROBADA')
                """
                + filtro.clausula();

        return jdbcTemplate.query(sql, filtro.parametros(), rs -> {
            if (!rs.next()) {
                return 0.0;
            }
            int cerradas = rs.getInt("cerradas");
            if (cerradas == 0) {
                return 0.0;
            }
            return (double) rs.getInt("aprobadas") / cerradas;
        });
    }

    public Map<String, Double> tasaAprobacionPorPrograma(
            Optional<PeriodoAcademicoUtil.PeriodoAcademico> periodo,
            Long facultadId) {

        FiltroSql filtro = filtroPracticasCerradas(periodo, facultadId);
        String sql = """
                SELECT prog.nombre,
                    COUNT(*) FILTER (WHERE ip.estado = 'COMPLETADA') AS aprobadas,
                    COUNT(*) FILTER (WHERE ip.estado IN ('COMPLETADA', 'REPROBADA')) AS cerradas
                """
                + JOIN_BASE
                + """
                WHERE ip.estado IN ('COMPLETADA', 'REPROBADA')
                """
                + filtro.clausula()
                + """
                 GROUP BY prog.id, prog.nombre
                 ORDER BY prog.nombre
                """;

        Map<String, Double> resultado = new LinkedHashMap<>();
        jdbcTemplate.query(sql, filtro.parametros(), rs -> {
            int cerradas = rs.getInt("cerradas");
            double tasa = cerradas == 0 ? 0.0 : (double) rs.getInt("aprobadas") / cerradas;
            resultado.put(rs.getString("nombre"), tasa);
        });
        return resultado;
    }

    public int contarEmpresasActivas(
            Optional<PeriodoAcademicoUtil.PeriodoAcademico> periodo,
            Long facultadId) {

        FiltroSql filtro = filtroPracticaActiva(periodo, facultadId);
        String sql = """
                SELECT COUNT(DISTINCT emp.id)
                FROM empresas emp
                WHERE emp.activo = TRUE
                  AND EXISTS (
                    SELECT 1
                """
                + JOIN_BASE
                + """
                    WHERE ip.empresa_id = emp.id
                      AND ip.estado = 'EN_CURSO'
                """
                + filtro.clausula()
                + """
                  )
                """;

        Integer total = jdbcTemplate.queryForObject(sql, filtro.parametros(), Integer.class);
        return total != null ? total : 0;
    }

    /**
     * Días promedio desde la primera asignación (inicio de gestión post-APTO) hasta {@code fecha_inicio}
     * en estado EN_CURSO (equivalente EN_PRACTICA del dominio).
     */
    public double tiempoPromedioGestionDias(
            Optional<PeriodoAcademicoUtil.PeriodoAcademico> periodo,
            Long facultadId) {

        FiltroSql filtro = filtroPracticaActiva(periodo, facultadId);
        String sql = """
                SELECT COALESCE(AVG(
                    EXTRACT(EPOCH FROM (ip.fecha_inicio::timestamp - gestion.fecha_inicio_gestion)) / 86400
                ), 0) AS promedio_dias
                FROM instancias_practica ip
                JOIN expedientes exp ON exp.id = ip.expediente_id
                JOIN estudiantes est ON est.id = exp.estudiante_id
                JOIN programas prog ON prog.id = est.programa_id
                JOIN facultades fac ON fac.id = prog.facultad_id
                JOIN LATERAL (
                    SELECT MIN(a.fecha_creacion) AS fecha_inicio_gestion
                    FROM asignaciones a
                    WHERE a.instancia_practica_id = ip.id
                ) gestion ON TRUE
                WHERE ip.estado IN ('EN_CURSO', 'COMPLETADA', 'REPROBADA')
                  AND ip.fecha_inicio IS NOT NULL
                  AND gestion.fecha_inicio_gestion IS NOT NULL
                  AND est.estado_aptitud = 'APTO'
                """
                + filtro.clausula();

        Double promedio = jdbcTemplate.queryForObject(sql, filtro.parametros(), Double.class);
        return promedio != null ? promedio : 0.0;
    }

    public int contarPracticasCerradasEnPeriodo(
            Optional<PeriodoAcademicoUtil.PeriodoAcademico> periodo,
            Long facultadId) {

        FiltroSql filtro = filtroPracticasCerradas(periodo, facultadId);
        String sql = """
                SELECT COUNT(*)
                """
                + JOIN_BASE
                + """
                WHERE ip.estado IN ('COMPLETADA', 'REPROBADA')
                """
                + filtro.clausula();

        Integer total = jdbcTemplate.queryForObject(sql, filtro.parametros(), Integer.class);
        return total != null ? total : 0;
    }

    private FiltroSql filtroPracticaActiva(
            Optional<PeriodoAcademicoUtil.PeriodoAcademico> periodo,
            Long facultadId) {
        return construirFiltro(periodo, facultadId, "ip.fecha_inicio");
    }

    private FiltroSql filtroPracticasCerradas(
            Optional<PeriodoAcademicoUtil.PeriodoAcademico> periodo,
            Long facultadId) {
        return construirFiltro(periodo, facultadId, "COALESCE(ip.fecha_fin, ip.fecha_inicio)");
    }

    private FiltroSql construirFiltro(
            Optional<PeriodoAcademicoUtil.PeriodoAcademico> periodo,
            Long facultadId,
            String columnaFecha) {

        List<Object> params = new ArrayList<>();
        StringBuilder clausula = new StringBuilder();

        if (facultadId != null) {
            clausula.append(" AND fac.id = ? ");
            params.add(facultadId);
        }

        periodo.ifPresent(p -> {
            clausula.append(" AND ").append(columnaFecha).append(" >= ? ");
            clausula.append(" AND ").append(columnaFecha).append(" <= ? ");
            params.add(p.fechaInicio());
            params.add(p.fechaFin());
        });

        return new FiltroSql(clausula.toString(), params.toArray());
    }

    private record FiltroSql(String clausula, Object[] parametros) {
    }
}
