ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS facultad_id BIGINT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_usuarios_facultad'
    ) THEN
        ALTER TABLE usuarios
            ADD CONSTRAINT fk_usuarios_facultad
            FOREIGN KEY (facultad_id) REFERENCES facultades(id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_usuarios_facultad ON usuarios(facultad_id);
