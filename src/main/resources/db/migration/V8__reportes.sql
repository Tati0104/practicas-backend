-- Tabla: jobs_exportacion
CREATE TABLE jobs_exportacion (
    id BIGSERIAL PRIMARY KEY,
    tipo_reporte VARCHAR(100) NOT NULL,
    estado VARCHAR(50) DEFAULT 'PENDIENTE' NOT NULL,
    url_archivo VARCHAR(255),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_job_estado CHECK (estado IN ('PENDIENTE', 'EN_PROCESO', 'COMPLETADO', 'FALLIDO'))
);

-- Índices
CREATE INDEX idx_jobs_exportacion_estado ON jobs_exportacion(estado);
