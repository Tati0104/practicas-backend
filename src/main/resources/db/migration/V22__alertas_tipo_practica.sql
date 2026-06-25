-- PE-37: columnas de alertas (idempotente; consolidado con V19 en ramas mergeadas)
ALTER TABLE alertas_sistema
    ADD COLUMN IF NOT EXISTS tipo VARCHAR(50),
    ADD COLUMN IF NOT EXISTS instancia_practica_id BIGINT;
