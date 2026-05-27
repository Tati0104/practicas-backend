-- KBM - Migración alineada con el modelo entregado en dbdiagram.
-- Se usan nombres reales de tablas y columnas: usuario, rol, docente_asesor, vacante, bitacora y plantilla_correo.
-- Command no se implementa. Se mantienen State, Observer, Factory Method, Proxy base y Mediator base.

CREATE TABLE IF NOT EXISTS rol (
    id_rol BIGSERIAL PRIMARY KEY,
    nombre_rol VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    permisos TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO rol (id_rol, nombre_rol, descripcion, permisos, activo)
VALUES
(1, 'ADMIN', 'Administrador del sistema', 'ALL', TRUE),
(2, 'DIRECCION', 'Dirección institucional', 'DASHBOARD,REPORTES', TRUE),
(3, 'COORD_ACADEMICA', 'Coordinación académica', 'ESTUDIANTES,PRACTICAS', TRUE),
(4, 'COORD_PRACTICA', 'Coordinación de prácticas', 'VACANTES,ASIGNACIONES,CIERRES', TRUE),
(5, 'SECRETARIA', 'Secretaría de coordinación empresarial', 'CONSULTA,REGISTRO', TRUE),
(6, 'DOCENTE_ASESOR', 'Docente asesor de prácticas', 'SEGUIMIENTO,CALIFICACION', TRUE),
(7, 'EMPRESA', 'Empresa formadora', 'VACANTES,TUTORES', TRUE),
(8, 'TUTOR_EMPRESARIAL', 'Tutor empresarial', 'SEGUIMIENTO,ENCUESTAS', TRUE),
(9, 'ESTUDIANTE', 'Estudiante en práctica', 'PRACTICA,DOCUMENTOS,ENCUESTAS', TRUE)
ON CONFLICT (id_rol) DO NOTHING;

SELECT setval(pg_get_serial_sequence('rol', 'id_rol'), GREATEST((SELECT MAX(id_rol) FROM rol), 9), true);

CREATE TABLE IF NOT EXISTS usuario (
    id_usuario BIGSERIAL PRIMARY KEY,
    nombre_completo VARCHAR(200) NOT NULL,
    correo VARCHAR(200) NOT NULL UNIQUE,
    contrasena_hash VARCHAR(255) NOT NULL,
    id_rol BIGINT NOT NULL REFERENCES rol(id_rol),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT NOW(),
    ultimo_acceso TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_usuario_correo ON usuario(correo);
CREATE INDEX IF NOT EXISTS idx_usuario_rol ON usuario(id_rol);

CREATE TABLE IF NOT EXISTS facultad (
    id_facultad BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS programa (
    id_programa BIGSERIAL PRIMARY KEY,
    id_facultad BIGINT REFERENCES facultad(id_facultad),
    nombre VARCHAR(150) NOT NULL,
    num_practicas INTEGER,
    num_cortes_por_practica INTEGER,
    nota_minima_aprobacion DECIMAL(4,2),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS empresa_formadora (
    id_empresa BIGSERIAL PRIMARY KEY,
    nit VARCHAR(20) UNIQUE,
    razon_social VARCHAR(200),
    sector_economico VARCHAR(100),
    direccion VARCHAR(200),
    municipio VARCHAR(100),
    telefono VARCHAR(20),
    nombre_contacto VARCHAR(150),
    estado VARCHAR(50),
    fecha_registro TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS docente_asesor (
    id_docente_asesor BIGSERIAL PRIMARY KEY,
    id_usuario BIGINT NOT NULL UNIQUE REFERENCES usuario(id_usuario),
    id_programa BIGINT NOT NULL REFERENCES programa(id_programa),
    area_conocimiento VARCHAR(150),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_docente_asesor_usuario ON docente_asesor(id_usuario);
CREATE INDEX IF NOT EXISTS idx_docente_asesor_programa ON docente_asesor(id_programa);
CREATE INDEX IF NOT EXISTS idx_docente_asesor_activo ON docente_asesor(activo);

CREATE TABLE IF NOT EXISTS vacante (
    id_vacante BIGSERIAL PRIMARY KEY,
    id_empresa BIGINT NOT NULL REFERENCES empresa_formadora(id_empresa),
    id_programa BIGINT NOT NULL REFERENCES programa(id_programa),
    id_creado_por BIGINT REFERENCES usuario(id_usuario),
    id_aprobado_por BIGINT REFERENCES usuario(id_usuario),
    cargo VARCHAR(200) NOT NULL,
    descripcion_perfil TEXT,
    requisitos TEXT,
    cupos_totales INTEGER NOT NULL,
    cupos_ocupados INTEGER NOT NULL DEFAULT 0,
    area VARCHAR(100),
    modalidad VARCHAR(50),
    fecha_inicio_disponibilidad DATE,
    fecha_fin_disponibilidad DATE,
    estado VARCHAR(50) NOT NULL,
    motivo_rechazo TEXT,
    CONSTRAINT chk_vacante_cupos CHECK (cupos_totales > 0 AND cupos_ocupados >= 0 AND cupos_ocupados <= cupos_totales)
);

CREATE INDEX IF NOT EXISTS idx_vacante_empresa ON vacante(id_empresa);
CREATE INDEX IF NOT EXISTS idx_vacante_programa ON vacante(id_programa);
CREATE INDEX IF NOT EXISTS idx_vacante_estado ON vacante(estado);
CREATE INDEX IF NOT EXISTS idx_vacante_modalidad ON vacante(modalidad);
CREATE INDEX IF NOT EXISTS idx_vacante_area ON vacante(area);

CREATE TABLE IF NOT EXISTS bitacora (
    id_bitacora BIGSERIAL PRIMARY KEY,
    id_usuario BIGINT REFERENCES usuario(id_usuario),
    modulo VARCHAR(100) NOT NULL,
    accion VARCHAR(100) NOT NULL,
    registro_afectado VARCHAR(100),
    valores_anteriores TEXT,
    valores_nuevos TEXT,
    fecha_hora TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_bitacora_usuario ON bitacora(id_usuario);
CREATE INDEX IF NOT EXISTS idx_bitacora_modulo ON bitacora(modulo);
CREATE INDEX IF NOT EXISTS idx_bitacora_accion ON bitacora(accion);
CREATE INDEX IF NOT EXISTS idx_bitacora_fecha ON bitacora(fecha_hora);

CREATE TABLE IF NOT EXISTS plantilla_correo (
    id_plantilla BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL UNIQUE,
    asunto VARCHAR(200) NOT NULL,
    cuerpo TEXT NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO plantilla_correo (nombre, asunto, cuerpo, activa)
VALUES
('NUEVA_ASIGNACION', 'Nueva asignación de práctica', '<h3>Nueva asignación</h3><p>Hola {{nombre_estudiante}}, tienes una nueva asignación con la empresa {{empresa}}.</p>', TRUE),
('CAMBIO_ESTADO', 'Cambio de estado', '<h3>Cambio de estado</h3><p>El proceso cambió al estado {{estado}}.</p>', TRUE),
('ENCUESTA_DISPONIBLE', 'Encuesta disponible', '<h3>Encuesta disponible</h3><p>Ya puedes diligenciar la encuesta de la práctica.</p>', TRUE),
('RECORDATORIO_ENCUESTA', 'Recordatorio de encuesta', '<h3>Recordatorio</h3><p>Recuerda diligenciar la encuesta pendiente.</p>', TRUE),
('ALERTA_INACTIVIDAD', 'Alerta de inactividad', '<h3>Alerta de inactividad</h3><p>La práctica de {{nombre_estudiante}} presenta inactividad.</p>', TRUE),
('CONFIRMACION_VINCULACION', 'Confirmación de vinculación', '<h3>Vinculación confirmada</h3><p>La vinculación con {{empresa}} fue confirmada.</p>', TRUE),
('RESULTADO_CIERRE', 'Resultado de cierre de práctica', '<h3>Resultado de cierre</h3><p>La práctica finalizó con resultado: {{resultado}}.</p>', TRUE),
('VACANTE_CREADA', 'Vacante creada', '<h3>Vacante creada</h3><p>La vacante {{cargo}} fue creada y quedó pendiente de aprobación.</p>', TRUE),
('VACANTE_APROBADA', 'Vacante aprobada', '<h3>Vacante aprobada</h3><p>La vacante {{cargo}} fue aprobada.</p>', TRUE),
('VACANTE_RECHAZADA', 'Vacante rechazada', '<h3>Vacante rechazada</h3><p>La vacante {{cargo}} fue rechazada.</p>', TRUE),
('DOCENTE_ASESOR_CREADO', 'Usuario docente asesor creado', '<h3>Usuario docente asesor</h3><p>Hola {{nombre}}, tu usuario docente asesor fue creado.</p>', TRUE)
ON CONFLICT (nombre) DO NOTHING;
