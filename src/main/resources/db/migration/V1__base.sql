-- Tabla: catalogo_items (Para catálogos maestros como SECTOR_ECONOMICO, AREA_PRACTICA, MODALIDAD, TIPO_DOCUMENTO)
CREATE TABLE catalogo_items (
    id BIGSERIAL PRIMARY KEY,
    tipo VARCHAR(50) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    activo BOOLEAN DEFAULT TRUE NOT NULL,
    CONSTRAINT chk_catalogo_items_tipo CHECK (tipo IN ('SECTOR_ECONOMICO', 'AREA_PRACTICA', 'MODALIDAD', 'TIPO_DOCUMENTO'))
);

-- Tabla: usuarios
CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    correo VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(50) NOT NULL,
    activo BOOLEAN DEFAULT TRUE NOT NULL,
    CONSTRAINT chk_usuarios_rol CHECK (rol IN ('ESTUDIANTE', 'DOCENTE_ASESOR', 'TUTOR_EMPRESARIAL', 'ADMIN'))
);

-- Tabla: facultades
CREATE TABLE facultades (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) UNIQUE NOT NULL,
    activo BOOLEAN DEFAULT TRUE NOT NULL
);

-- Tabla: programas
CREATE TABLE programas (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    facultad_id BIGINT NOT NULL,
    total_practicas INT DEFAULT 0 NOT NULL,
    activo BOOLEAN DEFAULT TRUE NOT NULL,
    CONSTRAINT fk_programas_facultad FOREIGN KEY (facultad_id) REFERENCES facultades(id)
);

-- Tabla: config_programas
CREATE TABLE config_programas (
    id BIGSERIAL PRIMARY KEY,
    programa_id BIGINT UNIQUE NOT NULL,
    num_practicas INT NOT NULL,
    num_cortes INT NOT NULL,
    nota_minima_aprobacion NUMERIC(3, 2) NOT NULL,
    max_asignaciones_simultaneas INT NOT NULL,
    umbral_inactividad_dias INT NOT NULL,
    CONSTRAINT fk_config_programas_programa FOREIGN KEY (programa_id) REFERENCES programas(id)
);

-- Tabla: catalogo_practicas
CREATE TABLE catalogo_practicas (
    id BIGSERIAL PRIMARY KEY,
    programa_id BIGINT NOT NULL,
    numero_practica INT NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    materia_nucleo VARCHAR(150) NOT NULL,
    codigo_materia VARCHAR(50) NOT NULL,
    num_cortes INT NOT NULL,
    duracion_semanas INT NOT NULL,
    activo BOOLEAN DEFAULT TRUE NOT NULL,
    CONSTRAINT fk_catalogo_practicas_programa FOREIGN KEY (programa_id) REFERENCES programas(id),
    CONSTRAINT uq_programa_numero_practica UNIQUE (programa_id, numero_practica)
);

-- Tabla: plantillas_correo
CREATE TABLE plantillas_correo (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(100) UNIQUE NOT NULL,
    asunto VARCHAR(255) NOT NULL,
    cuerpo TEXT NOT NULL
);

-- Índices para mejorar rendimiento de consultas recurrentes
CREATE INDEX idx_programas_facultad ON programas(facultad_id);
CREATE INDEX idx_config_programas_programa ON config_programas(programa_id);
CREATE INDEX idx_catalogo_practicas_programa ON catalogo_practicas(programa_id);
