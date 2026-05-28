-- Ajuste de tipo para alinear estudiantes.promedio_acumulado con la entidad Java (Double).
ALTER TABLE estudiantes
ALTER COLUMN promedio_acumulado TYPE DOUBLE PRECISION
USING promedio_acumulado::DOUBLE PRECISION;
