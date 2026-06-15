-- Columnas que existen en la entidad Asignacion pero faltaban en la tabla
-- (V6 creó la tabla sin ellas; se agregan aquí para sincronizar schema con el modelo)
ALTER TABLE asignaciones
    ADD COLUMN IF NOT EXISTS coordinador_id      BIGINT,
    ADD COLUMN IF NOT EXISTS nota_justificacion  TEXT,
    ADD COLUMN IF NOT EXISTS fecha_actualizacion TIMESTAMP,
    ADD COLUMN IF NOT EXISTS fecha_vinculacion   TIMESTAMP;
