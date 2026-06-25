ALTER TABLE alertas_sistema
    ADD COLUMN IF NOT EXISTS tipo VARCHAR(50),
    ADD COLUMN IF NOT EXISTS instancia_practica_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_alertas_tipo_practica_activa
    ON alertas_sistema(instancia_practica_id, tipo)
    WHERE resuelta = FALSE;
