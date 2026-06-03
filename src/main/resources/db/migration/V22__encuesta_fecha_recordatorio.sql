ALTER TABLE encuestas
    ADD COLUMN IF NOT EXISTS fecha_ultimo_recordatorio TIMESTAMP;
