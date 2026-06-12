#!/usr/bin/env python3
"""Genera la colección Postman AVH Practicas Backend."""
import json
from pathlib import Path

BASE = "{{baseUrl}}"

def req(name, method, path, body=None, query=None, auth=True, desc=""):
    raw_url = BASE + path
    if query:
        qs = "&".join(f"{k}={v}" for k, v in query.items())
        raw_url = f"{raw_url}?{qs}"
    item = {
        "name": name,
        "request": {
            "method": method,
            "header": [{"key": "Content-Type", "value": "application/json", "disabled": method in ("GET", "DELETE", "PATCH") and body is None}],
            "url": raw_url,
            "description": desc,
        },
    }
    if body is not None:
        item["request"]["body"] = {"mode": "raw", "raw": json.dumps(body, ensure_ascii=False, indent=2)}
    if not auth:
        item["request"]["auth"] = {"type": "noauth"}
    return item

def folder(name, items, desc=""):
    return {"name": name, "description": desc, "item": items}

login = req(
    "Login",
    "POST",
    "/auth/login",
    {"correo": "{{correo}}", "password": "{{password}}"},
    auth=False,
    desc="Obtiene JWT. El script de prueba guarda token en variable de colección.",
)
login["event"] = [
    {
        "listen": "test",
        "script": {
            "exec": [
                "if (pm.response.code === 200) {",
                "  const j = pm.response.json();",
                "  pm.collectionVariables.set('token', j.token);",
                "  if (j.rol) pm.collectionVariables.set('rol', j.rol);",
                "}",
            ],
            "type": "text/javascript",
        },
    }
]

collection = {
    "info": {
        "_postman_id": "avh-practicas-backend-2026",
        "name": "AVH Practicas Backend",
        "description": "Colección completa del API practicas-backend.\n\n1. Configurar variables: baseUrl, correo, password.\n2. Ejecutar Auth > Login.\n3. Endpoints Sprint 5/6 requieren rama EHS/sprint6-respaldo.\n\nGuía: docs/GUIA-PATRONES-Y-SOLID.md",
        "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json",
    },
    "auth": {
        "type": "bearer",
        "bearer": [{"key": "token", "value": "{{token}}", "type": "string"}],
    },
    "variable": [
        {"key": "baseUrl", "value": "http://localhost:8080"},
        {"key": "token", "value": ""},
        {"key": "correo", "value": "admin@avh.edu.co"},
        {"key": "password", "value": "Admin123!"},
        {"key": "estudianteId", "value": "1"},
        {"key": "practicaId", "value": "1"},
        {"key": "encuestaId", "value": "1"},
        {"key": "vacanteId", "value": "1"},
        {"key": "empresaId", "value": "1"},
        {"key": "programaId", "value": "1"},
        {"key": "facultadId", "value": "1"},
        {"key": "docenteId", "value": "1"},
        {"key": "tutorId", "value": "1"},
        {"key": "usuarioId", "value": "1"},
        {"key": "corte", "value": "1"},
        {"key": "observacionId", "value": "1"},
        {"key": "alertaId", "value": "1"},
    ],
    "item": [
        folder("Auth", [
            login,
            req("Cambiar password", "POST", "/auth/cambiar-password", {"id": 1, "nuevaPassword": "Nueva123!"}, auth=False),
            req("Recuperar password", "POST", "/auth/recuperar", {"correo": "{{correo}}"}, auth=False),
            req("Resetear password", "POST", "/auth/resetear", {"token": "token-reset", "nuevaPassword": "Nueva123!"}, auth=False),
        ]),
        folder("Admin - Usuarios", [
            req("Listar usuarios", "GET", "/admin/usuarios"),
            req("Crear usuario", "POST", "/admin/usuarios", {
                "correo": "nuevo@avh.edu.co", "nombre": "Usuario Nuevo", "password": "Pass123!",
                "rol": "COORD_PRACTICA", "scope": "PROGRAMA", "scopeId": 1, "activo": True
            }),
            req("Actualizar usuario", "PUT", "/admin/usuarios/{{usuarioId}}", {"nombre": "Usuario Actualizado", "activo": True}),
            req("Activar usuario", "PATCH", "/admin/usuarios/{{usuarioId}}/activar"),
            req("Inactivar usuario", "PATCH", "/admin/usuarios/{{usuarioId}}/inactivar"),
        ]),
        folder("Admin - Bitacora", [req("Consultar bitacora", "GET", "/admin/bitacora")]),
        folder("Admin - Plantillas correo", [
            req("Obtener plantilla", "GET", "/admin/plantillas/VACANTE_APROBADA"),
            req("Actualizar plantilla", "PUT", "/admin/plantillas/VACANTE_APROBADA", {
                "asunto": "Vacante aprobada", "cuerpoHtml": "<p>Hola {{nombre}}</p>"
            }),
            req("Preview plantilla", "POST", "/admin/plantillas/VACANTE_APROBADA/preview", {"variables": {"nombre": "Juan"}}),
        ]),
        folder("Configuracion - Facultades", [
            req("Listar facultades", "GET", "/facultades"),
            req("Obtener facultad", "GET", "/facultades/{{facultadId}}"),
            req("Crear facultad", "POST", "/facultades", {"nombre": "Ingenieria", "codigo": "ING", "activo": True}),
            req("Actualizar facultad", "PUT", "/facultades/{{facultadId}}", {"nombre": "Ingenieria", "codigo": "ING", "activo": True}),
            req("Desactivar facultad", "PATCH", "/facultades/{{facultadId}}/desactivar"),
            req("Activar facultad", "PATCH", "/facultades/{{facultadId}}/activar"),
        ]),
        folder("Configuracion - Programas", [
            req("Listar programas", "GET", "/programas"),
            req("Obtener programa", "GET", "/programas/{{programaId}}"),
            req("Programas por facultad", "GET", "/facultades/{{facultadId}}/programas"),
            req("Crear programa", "POST", "/programas", {
                "nombre": "Sistemas", "codigo": "SYS", "facultadId": 1, "activo": True
            }),
            req("Actualizar programa", "PUT", "/programas/{{programaId}}", {"nombre": "Sistemas", "codigo": "SYS", "activo": True}),
            req("Desactivar programa", "PATCH", "/programas/{{programaId}}/desactivar"),
            req("Activar programa", "PATCH", "/programas/{{programaId}}/activar"),
        ]),
        folder("Configuracion - Catalogos maestros", [
            req("Listar catalogos", "GET", "/catalogos"),
            req("Obtener catalogo", "GET", "/catalogos/1"),
            req("Crear item catalogo", "POST", "/catalogos", {"tipo": "SECTOR", "codigo": "TEC", "nombre": "Tecnologia", "activo": True}),
        ]),
        folder("Configuracion - Catalogo practicas", [
            req("Listar catalogo practicas", "GET", "/configuracion/catalogo", query={"programaId": "{{programaId}}"}),
            req("Obtener entrada", "GET", "/configuracion/catalogo/1"),
            req("Crear entrada", "POST", "/configuracion/catalogo", {
                "programaId": 1, "numeroPractica": 1, "nombre": "Practica profesional",
                "materiaNucleo": "Practica", "codigoMateria": "PRA101", "numCortes": 3, "activo": True
            }),
        ]),
        folder("Estudiantes", [
            req("Listar estudiantes", "GET", "/estudiantes", query={"page": "0", "size": "20"}),
            req("Obtener estudiante", "GET", "/estudiantes/{{estudianteId}}"),
            req("Crear estudiante", "POST", "/estudiantes", {
                "identificacion": "1001", "nombre": "Ana Perez", "correo": "ana@estudiante.edu",
                "programaId": 1, "semestre": 8, "creditosAprobados": 120, "promedioAcumulado": 4.0
            }),
            req("Actualizar estudiante", "PUT", "/estudiantes/{{estudianteId}}", {"nombre": "Ana Perez Actualizada"}),
            req("Marcar apto", "PATCH", "/estudiantes/{{estudianteId}}/aptitud"),
            req("Importar Excel", "POST", "/estudiantes/importar", desc="Usar form-data file en Postman (no JSON)"),
        ]),
        folder("Expedientes", [req("Obtener expediente", "GET", "/expedientes/{{estudianteId}}")]),
        folder("Docentes asesores", [
            req("Listar docentes", "GET", "/docentes-asesores"),
            req("Obtener docente", "GET", "/docentes-asesores/{{docenteId}}"),
            req("Crear docente", "POST", "/docentes-asesores", {
                "nombre": "Carlos Docente", "correo": "docente@avh.edu", "programaId": 1, "activo": True
            }),
        ]),
        folder("Empresas", [
            req("Listar empresas", "GET", "/empresas", query={"page": "0", "size": "20"}),
            req("Obtener empresa", "GET", "/empresas/{{empresaId}}"),
            req("Crear empresa", "POST", "/empresas", {
                "nit": "900123456", "razonSocial": "Empresa Demo SAS", "sectorId": 1,
                "direccion": "Calle 1", "municipio": "Medellin", "telefono": "3001234567"
            }),
            req("Tutores de empresa", "GET", "/empresas/{{empresaId}}/tutores"),
        ]),
        folder("Tutores empresariales", [
            req("Obtener tutor", "GET", "/tutores/{{tutorId}}"),
            req("Crear tutor", "POST", "/tutores", {
                "nombre": "Laura Tutor", "correo": "tutor@empresa.com", "empresaId": 1, "activo": True
            }),
        ]),
        folder("Vacantes", [
            req("Listar vacantes", "GET", "/vacantes", query={"page": "0", "size": "20"}),
            req("Obtener vacante", "GET", "/vacantes/{{vacanteId}}"),
            req("Crear vacante", "POST", "/vacantes", {
                "empresaId": 1, "programaId": 1, "cargo": "Desarrollador junior",
                "descripcion": "Practica en desarrollo", "modalidad": "PRESENCIAL",
                "cuposTotal": 2, "cuposDisponibles": 2
            }),
            req("Aprobar vacante", "PATCH", "/vacantes/{{vacanteId}}/aprobar"),
            req("Rechazar vacante", "PATCH", "/vacantes/{{vacanteId}}/rechazar", {"motivo": "No cumple requisitos"}),
            req("Pausar vacante", "PATCH", "/vacantes/{{vacanteId}}/pausar"),
            req("Reactivar vacante", "PATCH", "/vacantes/{{vacanteId}}/reactivar"),
            req("Cerrar vacante", "PATCH", "/vacantes/{{vacanteId}}/cerrar"),
            req("Descontar cupo", "PATCH", "/vacantes/{{vacanteId}}/descontar-cupo"),
            req("Liberar cupo", "PATCH", "/vacantes/{{vacanteId}}/liberar-cupo"),
        ]),
        folder("Dashboard", [
            req("Resumen dashboard", "GET", "/dashboard"),
            req("Alertas dashboard", "GET", "/dashboard/alertas"),
            req("Marcar alerta leida", "PATCH", "/dashboard/alertas/{{alertaId}}/leer"),
            req("Filtros disponibles", "GET", "/dashboard/filtros-disponibles"),
        ]),
        folder("Sprint 5 - Encuestas", [
            req("Obtener encuesta", "GET", "/encuestas/{{practicaId}}/ESTUDIANTE"),
            req("Guardar borrador", "POST", "/encuestas/{{encuestaId}}/borrador", {"respuestasJson": "{\"p1\":\"4\"}"}),
            req("Enviar encuesta", "POST", "/encuestas/{{encuestaId}}/enviar", {"respuestasJson": "{\"p1\":\"5\"}"}),
            req("Recordatorio encuesta", "POST", "/encuestas/{{practicaId}}/ESTUDIANTE/recordatorio"),
        ], "Rama EHS/sprint5-encuestas o sprint6-respaldo"),
        folder("Sprint 5 - Seguimiento", [
            req("Tablero seguimiento", "GET", "/seguimiento/tablero", query={
                "programaId": "{{programaId}}", "corte": "{{corte}}"
            }),
            req("Registrar observacion", "POST", "/seguimiento/{{practicaId}}/observaciones", {
                "observacion": "Buen avance", "visibleParaEstudiante": True
            }, query={"corte": "{{corte}}"}),
            req("Editar observacion", "PUT", "/seguimiento/observaciones/{{observacionId}}", {
                "observacion": "Observacion editada", "visibleParaEstudiante": True
            }),
            req("Registrar avance tutor", "POST", "/seguimiento/{{practicaId}}/avances-tutor", {
                "avance": "Cumple objetivos del mes", "logros": "API REST", "dificultades": "Ninguna"
            }, query={"corte": "{{corte}}"}),
            req("Registrar bitacora estudiante", "POST", "/seguimiento/{{practicaId}}/bitacora", {
                "descripcion": "Desarrollo de APIs y pruebas unitarias"
            }, query={"corte": "{{corte}}"}),
            req("Listar observaciones", "GET", "/seguimiento/{{practicaId}}/observaciones"),
            req("Listar avances tutor", "GET", "/seguimiento/{{practicaId}}/avances-tutor"),
            req("Listar bitacora", "GET", "/seguimiento/{{practicaId}}/bitacora"),
            req("Alertas sistema", "GET", "/seguimiento/alertas"),
        ], "Rama EHS/sprint5-seguimiento o sprint6-respaldo"),
        folder("Sprint 5 - Calificaciones", [
            req("Nota docente", "POST", "/calificaciones/{{practicaId}}/docente", {"nota": 4.5, "observaciones": "Excelente"}, query={"corte": "{{corte}}"}),
            req("Nota tutor", "POST", "/calificaciones/{{practicaId}}/tutor", {"nota": 4.0, "observaciones": "Cumple"}, query={"corte": "{{corte}}"}),
            req("Nota final", "POST", "/calificaciones/{{practicaId}}/final", {"notaFinal": 4.3}),
            req("Resumen calificaciones", "GET", "/calificaciones/{{practicaId}}/resumen"),
        ], "Rama EHS/sprint5-calificaciones o sprint6-respaldo"),
        folder("Sprint 6 - Respaldo", [
            req("Generar respaldo Excel", "GET", "/respaldo/generar", desc="COORD_PRACTICA o ADMIN. Puede retornar 200 (archivo) o 202 (background)."),
        ], "Solo rama EHS/sprint6-respaldo"),
    ],
}

out = Path(__file__).parent / "AVH-Practicas-Backend.postman_collection.json"
out.write_text(json.dumps(collection, ensure_ascii=False, indent=2), encoding="utf-8")
print(f"Written {out} ({out.stat().st_size} bytes)")
