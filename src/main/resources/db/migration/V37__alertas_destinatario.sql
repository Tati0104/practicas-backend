-- Destinatario explícito para alertas personales (campanita por usuario)
ALTER TABLE alertas_sistema
    ADD COLUMN IF NOT EXISTS destinatario_correo VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_alertas_destinatario ON alertas_sistema(destinatario_correo);
CREATE INDEX IF NOT EXISTS idx_alertas_practica_leida ON alertas_sistema(instancia_practica_id, leida);
