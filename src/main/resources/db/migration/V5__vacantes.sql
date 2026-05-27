-- Tabla: vacantes
CREATE TABLE vacantes (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    programa_id BIGINT NOT NULL,
    cargo VARCHAR(150) NOT NULL,
    descripcion TEXT NOT NULL,
    perfil_requisitos TEXT,
    modalidad VARCHAR(50) NOT NULL,
    cupos_total INT NOT NULL,
    cupos_disponibles INT NOT NULL,
    estado VARCHAR(50) DEFAULT 'PENDIENTE_APROBACION' NOT NULL,
    fecha_disponibilidad_inicio DATE,
    fecha_disponibilidad_fin DATE,
    CONSTRAINT fk_vacantes_empresas FOREIGN KEY (empresa_id) REFERENCES empresas(id),
    CONSTRAINT fk_vacantes_programas FOREIGN KEY (programa_id) REFERENCES programas(id),
    CONSTRAINT chk_vacante_estado CHECK (estado IN ('PENDIENTE_APROBACION', 'ACTIVA', 'PAUSADA', 'CUPOS_COMPLETOS', 'CERRADA'))
);

-- Índices
CREATE INDEX idx_vacantes_empresa ON vacantes(empresa_id);
CREATE INDEX idx_vacantes_programa ON vacantes(programa_id);
CREATE INDEX idx_vacantes_estado ON vacantes(estado);
