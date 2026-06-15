ALTER TABLE empresas
    ADD COLUMN IF NOT EXISTS motivo_inactivacion TEXT,
    ADD COLUMN IF NOT EXISTS fecha_inactivacion TIMESTAMP;
