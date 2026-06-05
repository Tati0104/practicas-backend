-- PE-37: tipo e instancia de práctica para alertas generadas por vinculación
ALTER TABLE alertas_sistema
    ADD COLUMN IF NOT EXISTS tipo VARCHAR(50),
    ADD COLUMN IF NOT EXISTS instancia_practica_id BIGINT;
