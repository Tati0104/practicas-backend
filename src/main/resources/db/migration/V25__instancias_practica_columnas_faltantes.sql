-- Columnas que existen en la entidad InstanciaPractica pero faltaban en la tabla
-- (V2 creó la tabla sin ellas; se agregan aquí para sincronizar schema con el modelo)
ALTER TABLE instancias_practica
    ADD COLUMN IF NOT EXISTS codigo_materia   VARCHAR(50) NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS num_cortes       INT        NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS duracion_semanas INT        NOT NULL DEFAULT 0;
