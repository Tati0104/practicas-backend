-- KBM - Complemento para PE-26/PE-27/PE-28 VacanteService y patrón State.
-- EHS crea vacantes en V5. Este script agrega campos de auditoría y permite estado RECHAZADA.

ALTER TABLE vacantes
    ADD COLUMN IF NOT EXISTS creado_por_id BIGINT,
    ADD COLUMN IF NOT EXISTS aprobado_por_id BIGINT,
    ADD COLUMN IF NOT EXISTS motivo_rechazo TEXT,
    ADD COLUMN IF NOT EXISTS area VARCHAR(150);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_vacante_estado'
    ) THEN
        ALTER TABLE vacantes DROP CONSTRAINT chk_vacante_estado;
    END IF;
END $$;

ALTER TABLE vacantes
    ADD CONSTRAINT chk_vacante_estado
    CHECK (estado IN ('PENDIENTE_APROBACION', 'ACTIVA', 'PAUSADA', 'CUPOS_COMPLETOS', 'CERRADA', 'RECHAZADA'));

CREATE INDEX IF NOT EXISTS idx_vacantes_area ON vacantes(area);
