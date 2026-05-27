-- Tabla: docentes_asesores
CREATE TABLE docentes_asesores (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT UNIQUE NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    correo VARCHAR(150) UNIQUE NOT NULL,
    telefono VARCHAR(50),
    activo BOOLEAN DEFAULT TRUE NOT NULL,
    CONSTRAINT fk_docentes_usuarios FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- Tabla: estudiantes
CREATE TABLE estudiantes (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT UNIQUE,
    identificacion VARCHAR(50) UNIQUE NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    correo VARCHAR(150) UNIQUE NOT NULL,
    telefono VARCHAR(50),
    contacto_emergencia VARCHAR(255),
    programa_id BIGINT NOT NULL,
    semestre INT NOT NULL,
    creditos_aprobados INT DEFAULT 0 NOT NULL,
    promedio_acumulado NUMERIC(3, 2) DEFAULT 0.00 NOT NULL,
    estado_aptitud VARCHAR(30) DEFAULT 'SIN_EVALUAR' NOT NULL,
    CONSTRAINT fk_estudiantes_usuarios FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CONSTRAINT fk_estudiantes_programa FOREIGN KEY (programa_id) REFERENCES programas(id),
    CONSTRAINT chk_estudiantes_aptitud CHECK (estado_aptitud IN ('SIN_EVALUAR', 'APTO', 'NO_APTO'))
);

-- Tabla: expedientes
CREATE TABLE expedientes (
    id BIGSERIAL PRIMARY KEY,
    estudiante_id BIGINT UNIQUE NOT NULL,
    CONSTRAINT fk_expedientes_estudiantes FOREIGN KEY (estudiante_id) REFERENCES estudiantes(id)
);

-- Tabla: instancias_practica (Se relacionará con empresas y tutores en V4)
CREATE TABLE instancias_practica (
    id BIGSERIAL PRIMARY KEY,
    expediente_id BIGINT NOT NULL,
    numero_practica INT NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    materia_nucleo VARCHAR(150) NOT NULL,
    estado VARCHAR(50) DEFAULT 'ASIGNADA_PENDIENTE_INICIO' NOT NULL,
    empresa_id BIGINT, -- Se añadirá FK en V4
    docente_asesor_id BIGINT,
    tutor_id BIGINT, -- Se añadirá FK en V4
    fecha_inicio DATE,
    fecha_fin DATE,
    inmutable BOOLEAN DEFAULT FALSE NOT NULL,
    CONSTRAINT fk_instancias_expedientes FOREIGN KEY (expediente_id) REFERENCES expedientes(id),
    CONSTRAINT fk_instancias_docentes FOREIGN KEY (docente_asesor_id) REFERENCES docentes_asesores(id),
    CONSTRAINT chk_instancia_estado CHECK (estado IN ('ASIGNADA_PENDIENTE_INICIO', 'EN_CURSO', 'COMPLETADA', 'REPROBADA', 'CANCELADA'))
);

-- Índices
CREATE INDEX idx_docentes_usuario ON docentes_asesores(usuario_id);
CREATE INDEX idx_estudiantes_usuario ON estudiantes(usuario_id);
CREATE INDEX idx_estudiantes_programa ON estudiantes(programa_id);
CREATE INDEX idx_expedientes_estudiante ON expedientes(estudiante_id);
CREATE INDEX idx_instancias_expediente ON instancias_practica(expediente_id);
CREATE INDEX idx_instancias_docente ON instancias_practica(docente_asesor_id);
