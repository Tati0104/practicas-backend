-- PE: Archivos adjuntos en bitácora del estudiante
-- Agrega nombre y ruta del archivo de soporte subido por el estudiante.
-- Ambas columnas son nullable: las bitácoras sin adjunto las dejan en NULL.

ALTER TABLE bitacora_estudiante
    ADD COLUMN IF NOT EXISTS nombre_archivo VARCHAR(255),
    ADD COLUMN IF NOT EXISTS url_archivo    VARCHAR(512);
