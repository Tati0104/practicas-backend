-- KBM - Complemento para PE-17 apoyo DocenteAsesorService.
-- EHS crea docentes_asesores en V2 con usuario_id, nombre, correo, telefono y activo.
-- Este complemento agrega programa_id y area_conocimiento porque el sprint exige listarPorPrograma().

ALTER TABLE docentes_asesores
    ADD COLUMN IF NOT EXISTS programa_id BIGINT,
    ADD COLUMN IF NOT EXISTS area_conocimiento VARCHAR(150);

ALTER TABLE docentes_asesores
    ADD CONSTRAINT fk_docentes_asesores_programa
    FOREIGN KEY (programa_id) REFERENCES programas(id);

CREATE INDEX IF NOT EXISTS idx_docentes_asesores_programa ON docentes_asesores(programa_id);
