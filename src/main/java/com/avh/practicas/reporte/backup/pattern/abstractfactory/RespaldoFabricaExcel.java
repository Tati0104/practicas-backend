package com.avh.practicas.reporte.backup.pattern.abstractfactory;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Fábrica concreta de Excel para recopilar la información del respaldo completo (Abstract Factory Pattern - ConcreteFactory).
 * Consulta la base de datos de manera agnóstica y unificada utilizando JdbcTemplate.
 */
@Component
@RequiredArgsConstructor
public class RespaldoFabricaExcel implements FabricaExcel {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<String> obtenerEncabezados(String nombreHoja) {
        return switch (nombreHoja) {
            case "Usuarios" -> List.of("ID", "Nombre", "Correo", "Rol", "Scope", "Activo");
            case "Estudiantes" -> List.of("ID", "Identificación", "Nombre", "Correo", "Teléfono", "Contacto Emergencia", "Programa ID", "Semestre", "Créditos Aprobados", "Promedio Acumulado", "Estado Aptitud");
            case "Expedientes con prácticas" -> List.of("Expediente ID", "Estudiante ID", "Práctica ID", "Número Práctica", "Nombre Práctica", "Materia", "Estado Práctica", "Empresa ID", "Docente ID", "Tutor ID", "Fecha Inicio", "Fecha Fin", "Inmutable");
            case "Empresas" -> List.of("ID", "NIT", "Razón Social", "Sector ID", "Dirección", "Municipio", "Teléfono", "Activo");
            case "Vacantes" -> List.of("ID", "Empresa ID", "Programa ID", "Cargo", "Descripción", "Modalidad", "Cupos Total", "Cupos Disponibles", "Estado");
            case "Notas registradas" -> List.of("Práctica ID", "Tipo Nota (DOCENTE/TUTOR/FINAL)", "Nota", "Corte", "Fecha", "Observaciones");
            case "Evaluaciones y encuestas" -> List.of("ID", "Práctica ID", "Tipo Encuesta", "Estado", "Fecha Envío", "Respuestas JSON");
            case "Bitácora últimos 2 años" -> List.of("ID / Práctica ID", "Detalle / Mensaje", "Fecha", "Tipo Registro");
            default -> new ArrayList<>();
        };
    }

    @Override
    public List<List<Object>> obtenerDatos(String nombreHoja) {
        List<List<Object>> filas = new ArrayList<>();

        switch (nombreHoja) {
            case "Usuarios":
                jdbcTemplate.query("SELECT id, nombre, correo, rol, scope, activo FROM usuarios ORDER BY id", rs -> {
                    List<Object> fila = List.of(
                            rs.getLong("id"),
                            rs.getString("nombre"),
                            rs.getString("correo"),
                            rs.getString("rol"),
                            rs.getString("scope"),
                            rs.getBoolean("activo")
                    );
                    filas.add(fila);
                });
                break;

            case "Estudiantes":
                jdbcTemplate.query("SELECT id, identificacion, nombre, correo, telefono, contacto_emergencia, programa_id, semestre, creditos_aprobados, promedio_acumulado, estado_aptitud FROM estudiantes ORDER BY id", rs -> {
                    List<Object> fila = List.of(
                            rs.getLong("id"),
                            rs.getString("identificacion"),
                            rs.getString("nombre"),
                            rs.getString("correo"),
                            rs.getString("telefono") != null ? rs.getString("telefono") : "",
                            rs.getString("contacto_emergencia") != null ? rs.getString("contacto_emergencia") : "",
                            rs.getLong("programa_id"),
                            rs.getInt("semestre"),
                            rs.getInt("creditos_aprobados"),
                            rs.getDouble("promedio_acumulado"),
                            rs.getString("estado_aptitud")
                    );
                    filas.add(fila);
                });
                break;

            case "Expedientes con prácticas":
                jdbcTemplate.query("SELECT e.id AS exp_id, e.estudiante_id, ip.id AS ip_id, ip.numero_practica, ip.nombre, ip.materia_nucleo, ip.estado, ip.empresa_id, ip.docente_asesor_id, ip.tutor_id, ip.fecha_inicio, ip.fecha_fin, ip.inmutable FROM expedientes e LEFT JOIN instancias_practica ip ON e.id = ip.expediente_id ORDER BY e.id", rs -> {
                    List<Object> fila = List.of(
                            rs.getLong("exp_id"),
                            rs.getLong("estudiante_id"),
                            rs.getObject("ip_id") != null ? rs.getLong("ip_id") : "",
                            rs.getObject("numero_practica") != null ? rs.getInt("numero_practica") : "",
                            rs.getString("nombre") != null ? rs.getString("nombre") : "",
                            rs.getString("materia_nucleo") != null ? rs.getString("materia_nucleo") : "",
                            rs.getString("estado") != null ? rs.getString("estado") : "",
                            rs.getObject("empresa_id") != null ? rs.getLong("empresa_id") : "",
                            rs.getObject("docente_asesor_id") != null ? rs.getLong("docente_asesor_id") : "",
                            rs.getObject("tutor_id") != null ? rs.getLong("tutor_id") : "",
                            rs.getDate("fecha_inicio") != null ? rs.getDate("fecha_inicio").toString() : "",
                            rs.getDate("fecha_fin") != null ? rs.getDate("fecha_fin").toString() : "",
                            rs.getObject("inmutable") != null ? rs.getBoolean("inmutable") : ""
                    );
                    filas.add(fila);
                });
                break;

            case "Empresas":
                jdbcTemplate.query("SELECT id, nit, razon_social, sector_id, direccion, municipio, telefono, activo FROM empresas ORDER BY id", rs -> {
                    List<Object> fila = List.of(
                            rs.getLong("id"),
                            rs.getString("nit"),
                            rs.getString("razon_social"),
                            rs.getLong("sector_id"),
                            rs.getString("direccion") != null ? rs.getString("direccion") : "",
                            rs.getString("municipio") != null ? rs.getString("municipio") : "",
                            rs.getString("telefono") != null ? rs.getString("telefono") : "",
                            rs.getBoolean("activo")
                    );
                    filas.add(fila);
                });
                break;

            case "Vacantes":
                jdbcTemplate.query("SELECT id, empresa_id, programa_id, cargo, descripcion, modalidad, cupos_total, cupos_disponibles, estado FROM vacantes ORDER BY id", rs -> {
                    List<Object> fila = List.of(
                            rs.getLong("id"),
                            rs.getLong("empresa_id"),
                            rs.getLong("programa_id"),
                            rs.getString("cargo"),
                            rs.getString("descripcion"),
                            rs.getString("modalidad") != null ? rs.getString("modalidad") : "",
                            rs.getInt("cupos_total"),
                            rs.getInt("cupos_disponibles"),
                            rs.getString("estado")
                    );
                    filas.add(fila);
                });
                break;

            case "Notas registradas":
                jdbcTemplate.query(
                    "SELECT instancia_practica_id, 'DOCENTE' as tipo, nota, corte, fecha, observaciones FROM notas_docente " +
                    "UNION ALL " +
                    "SELECT instancia_practica_id, 'TUTOR' as tipo, nota, corte, fecha, observaciones FROM notas_tutor " +
                    "UNION ALL " +
                    "SELECT instancia_practica_id, 'FINAL' as tipo, nota_final, -1, fecha, '' as observaciones FROM notas_finales " +
                    "ORDER BY instancia_practica_id, tipo", rs -> {
                        List<Object> fila = List.of(
                                rs.getLong("instancia_practica_id"),
                                rs.getString("tipo"),
                                rs.getDouble("nota"),
                                rs.getInt("corte") == -1 ? "N/A" : rs.getInt("corte"),
                                rs.getTimestamp("fecha").toString(),
                                rs.getString("observaciones") != null ? rs.getString("observaciones") : ""
                        );
                        filas.add(fila);
                    }
                );
                break;

            case "Evaluaciones y encuestas":
                jdbcTemplate.query("SELECT id, instancia_practica_id, tipo, estado, fecha_envio_invitacion, respuestas_json FROM encuestas ORDER BY id", rs -> {
                    List<Object> fila = List.of(
                            rs.getLong("id"),
                            rs.getLong("instancia_practica_id"),
                            rs.getString("tipo"),
                            rs.getString("estado"),
                            rs.getTimestamp("fecha_envio_invitacion").toString(),
                            rs.getString("respuestas_json") != null ? rs.getString("respuestas_json") : "{}"
                    );
                    filas.add(fila);
                });
                break;

            case "Bitácora últimos 2 años":
                LocalDateTime limite = LocalDateTime.now().minusYears(2);
                jdbcTemplate.query(
                    "SELECT instancia_practica_id, descripcion as msg, fecha, 'BITACORA_ESTUDIANTE' as tipo FROM bitacora_estudiante WHERE fecha >= ? " +
                    "UNION ALL " +
                    "SELECT instancia_practica_id, observacion as msg, fecha, 'OBSERVACION_DOCENTE' as tipo FROM observaciones_docente WHERE fecha >= ? " +
                    "UNION ALL " +
                    "SELECT instancia_practica_id, avance as msg, fecha, 'AVANCE_TUTOR' as tipo FROM avances_tutor WHERE fecha >= ? " +
                    "UNION ALL " +
                    "SELECT id as instancia_practica_id, mensaje as msg, fecha, 'ALERTA_SISTEMA' as tipo FROM alertas_sistema WHERE fecha >= ? " +
                    "UNION ALL " +
                    "SELECT usuario_id as instancia_practica_id, detalle as msg, fecha, 'BITACORA_AUDITORIA' as tipo FROM bitacora_auditoria WHERE fecha >= ? " +
                    "ORDER BY fecha DESC",
                    ps -> {
                        ps.setTimestamp(1, java.sql.Timestamp.valueOf(limite));
                        ps.setTimestamp(2, java.sql.Timestamp.valueOf(limite));
                        ps.setTimestamp(3, java.sql.Timestamp.valueOf(limite));
                        ps.setTimestamp(4, java.sql.Timestamp.valueOf(limite));
                        ps.setTimestamp(5, java.sql.Timestamp.valueOf(limite));
                    },
                    rs -> {
                        List<Object> fila = List.of(
                                rs.getLong("instancia_practica_id"),
                                rs.getString("msg") != null ? rs.getString("msg") : "",
                                rs.getTimestamp("fecha").toString(),
                                rs.getString("tipo")
                        );
                        filas.add(fila);
                    }
                );
                break;
        }

        return filas;
    }
}
