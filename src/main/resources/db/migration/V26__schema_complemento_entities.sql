-- PE-31: trazabilidad de cambios de estado en asignaciones
CREATE TABLE IF NOT EXISTS historial_asignaciones (
    id BIGSERIAL PRIMARY KEY,
    asignacion_id BIGINT NOT NULL,
    estado_anterior VARCHAR(50),
    estado_nuevo VARCHAR(50) NOT NULL,
    responsable_id BIGINT,
    motivo TEXT,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_historial_asignaciones_asignacion
        FOREIGN KEY (asignacion_id) REFERENCES asignaciones(id)
);

CREATE INDEX IF NOT EXISTS idx_historial_asignaciones_asignacion
    ON historial_asignaciones(asignacion_id);

-- Campos de InstanciaPractica alineados con catalogo_practicas
ALTER TABLE instancias_practica
    ADD COLUMN IF NOT EXISTS codigo_materia VARCHAR(50),
    ADD COLUMN IF NOT EXISTS num_cortes INT,
    ADD COLUMN IF NOT EXISTS duracion_semanas INT;

UPDATE instancias_practica
SET codigo_materia = COALESCE(codigo_materia, 'N/A'),
    num_cortes = COALESCE(num_cortes, 3),
    duracion_semanas = COALESCE(duracion_semanas, 16)
WHERE codigo_materia IS NULL
   OR num_cortes IS NULL
   OR duracion_semanas IS NULL;

ALTER TABLE instancias_practica
    ALTER COLUMN codigo_materia SET NOT NULL,
    ALTER COLUMN num_cortes SET NOT NULL,
    ALTER COLUMN duracion_semanas SET NOT NULL;

-- Campo usado por JobExportacion
ALTER TABLE jobs_exportacion
    ADD COLUMN IF NOT EXISTS mensaje TEXT;
