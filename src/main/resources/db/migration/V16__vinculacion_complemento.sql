-- PE-32 / PE-33: documentos por categoría, convenio vinculado a asignación y firmas

ALTER TABLE documentos_practica
    ADD COLUMN IF NOT EXISTS categoria VARCHAR(50),
    ADD COLUMN IF NOT EXISTS asignacion_id BIGINT;

ALTER TABLE asignaciones
    ADD COLUMN IF NOT EXISTS instancia_practica_id BIGINT;

ALTER TABLE asignaciones
    ADD CONSTRAINT fk_asignaciones_instancia_practica
        FOREIGN KEY (instancia_practica_id) REFERENCES instancias_practica(id);

ALTER TABLE convenios
    ADD COLUMN IF NOT EXISTS asignacion_id BIGINT,
    ADD COLUMN IF NOT EXISTS instancia_practica_id BIGINT,
    ADD COLUMN IF NOT EXISTS firma_coordinador_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS firma_tutor_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS firma_estudiante_at TIMESTAMP;

ALTER TABLE convenios
    ADD CONSTRAINT fk_convenios_asignacion
        FOREIGN KEY (asignacion_id) REFERENCES asignaciones(id);

ALTER TABLE convenios
    ADD CONSTRAINT fk_convenios_instancia_practica
        FOREIGN KEY (instancia_practica_id) REFERENCES instancias_practica(id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_convenios_asignacion ON convenios(asignacion_id)
    WHERE asignacion_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_documentos_categoria ON documentos_practica(categoria);
CREATE INDEX IF NOT EXISTS idx_documentos_asignacion ON documentos_practica(asignacion_id);
