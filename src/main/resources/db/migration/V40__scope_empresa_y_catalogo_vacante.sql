ALTER TABLE empresas
    ADD COLUMN IF NOT EXISTS usuario_id BIGINT;

ALTER TABLE empresas
    ADD CONSTRAINT fk_empresas_usuarios
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_empresas_usuario
    ON empresas(usuario_id)
    WHERE usuario_id IS NOT NULL;

ALTER TABLE vacantes
    ADD COLUMN IF NOT EXISTS catalogo_practica_id BIGINT;

ALTER TABLE vacantes
    ADD CONSTRAINT fk_vacantes_catalogo_practicas
        FOREIGN KEY (catalogo_practica_id) REFERENCES catalogo_practicas(id);

CREATE INDEX IF NOT EXISTS idx_vacantes_catalogo_practica
    ON vacantes(catalogo_practica_id);
