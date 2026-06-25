-- Columnas usadas por la entidad Asignacion pero ausentes en migraciones anteriores

ALTER TABLE asignaciones
    ADD COLUMN IF NOT EXISTS coordinador_id BIGINT,
    ADD COLUMN IF NOT EXISTS nota_justificacion TEXT,
    ADD COLUMN IF NOT EXISTS fecha_actualizacion TIMESTAMP,
    ADD COLUMN IF NOT EXISTS fecha_vinculacion TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_asignaciones_coordinador ON asignaciones(coordinador_id);
