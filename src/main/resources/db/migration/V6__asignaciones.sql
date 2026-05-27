-- Tabla: asignaciones
CREATE TABLE asignaciones (
    id BIGSERIAL PRIMARY KEY,
    vacante_id BIGINT NOT NULL,
    estudiante_id BIGINT NOT NULL,
    estado VARCHAR(50) DEFAULT 'ASIGNADA' NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_asignaciones_vacantes FOREIGN KEY (vacante_id) REFERENCES vacantes(id),
    CONSTRAINT fk_asignaciones_estudiantes FOREIGN KEY (estudiante_id) REFERENCES estudiantes(id),
    CONSTRAINT chk_asignacion_estado CHECK (estado IN ('ASIGNADA', 'EN_PROCESO_VINCULACION', 'VINCULADA', 'CANCELADA'))
);

-- Tabla: documentos_practica
CREATE TABLE documentos_practica (
    id BIGSERIAL PRIMARY KEY,
    instancia_practica_id BIGINT NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    url VARCHAR(255) NOT NULL,
    tipo VARCHAR(100) NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_documentos_instancias FOREIGN KEY (instancia_practica_id) REFERENCES instancias_practica(id)
);

-- Tabla: convenios
CREATE TABLE convenios (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    estado VARCHAR(50) DEFAULT 'ACTIVO' NOT NULL,
    url_documento VARCHAR(255),
    CONSTRAINT fk_convenios_empresas FOREIGN KEY (empresa_id) REFERENCES empresas(id),
    CONSTRAINT chk_convenio_estado CHECK (estado IN ('ACTIVO', 'VENCIDO', 'RESCINDIDO'))
);

-- Índices
CREATE INDEX idx_asignaciones_vacante ON asignaciones(vacante_id);
CREATE INDEX idx_asignaciones_estudiante ON asignaciones(estudiante_id);
CREATE INDEX idx_documentos_instancia ON documentos_practica(instancia_practica_id);
CREATE INDEX idx_convenios_empresa ON convenios(empresa_id);
