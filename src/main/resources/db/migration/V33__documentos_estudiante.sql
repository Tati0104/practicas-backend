CREATE TABLE IF NOT EXISTS documentos_estudiante (
    id BIGSERIAL PRIMARY KEY,
    estudiante_id BIGINT NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    url VARCHAR(255) NOT NULL,
    tipo VARCHAR(100) NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_documentos_estudiante_estudiantes FOREIGN KEY (estudiante_id) REFERENCES estudiantes (id) ON DELETE CASCADE
);
