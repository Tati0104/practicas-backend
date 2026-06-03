-- Migración para Sprint 5: Agregar campos faltantes a las entidades de seguimiento

ALTER TABLE observaciones_docente
    ADD COLUMN docente_id BIGINT,
    ADD COLUMN corte INT,
    ADD COLUMN visible_para_estudiante BOOLEAN DEFAULT TRUE NOT NULL;

ALTER TABLE observaciones_docente
    ADD CONSTRAINT fk_observaciones_docente_docente FOREIGN KEY (docente_id) REFERENCES docentes_asesores(id);

ALTER TABLE avances_tutor
    ADD COLUMN tutor_id BIGINT,
    ADD COLUMN corte INT,
    ADD COLUMN logros TEXT,
    ADD COLUMN dificultades TEXT;

ALTER TABLE avances_tutor
    ADD CONSTRAINT fk_avances_tutor_tutor FOREIGN KEY (tutor_id) REFERENCES tutores_empresariales(id);

ALTER TABLE bitacora_estudiante
    ADD COLUMN estudiante_id BIGINT,
    ADD COLUMN corte INT;

ALTER TABLE bitacora_estudiante
    ADD CONSTRAINT fk_bitacora_estudiante_estudiante FOREIGN KEY (estudiante_id) REFERENCES estudiantes(id);
