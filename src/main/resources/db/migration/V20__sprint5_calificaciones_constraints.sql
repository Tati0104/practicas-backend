-- Migración para Sprint 5: Agregar restricciones y campos en módulo de calificaciones

-- Asegurar que solo exista una nota de docente por corte en cada práctica
ALTER TABLE notas_docente
    ADD CONSTRAINT uq_notas_docente_instancia_corte UNIQUE (instancia_practica_id, corte);

-- Asegurar que solo exista una nota de tutor por corte en cada práctica
ALTER TABLE notas_tutor
    ADD CONSTRAINT uq_notas_tutor_instancia_corte UNIQUE (instancia_practica_id, corte);

-- Agregar columna observaciones para las notas si no existen
ALTER TABLE notas_docente
    ADD COLUMN IF NOT EXISTS observaciones TEXT;

ALTER TABLE notas_tutor
    ADD COLUMN IF NOT EXISTS observaciones TEXT;
