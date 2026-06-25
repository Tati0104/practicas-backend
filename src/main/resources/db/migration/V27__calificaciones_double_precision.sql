-- Alinear columnas de calificación con entidades Java (Double / DOUBLE PRECISION).
-- Hibernate validate rechaza NUMERIC cuando la entidad usa Double.

ALTER TABLE notas_docente
    ALTER COLUMN nota TYPE DOUBLE PRECISION
    USING nota::DOUBLE PRECISION;

ALTER TABLE notas_tutor
    ALTER COLUMN nota TYPE DOUBLE PRECISION
    USING nota::DOUBLE PRECISION;

ALTER TABLE notas_finales
    ALTER COLUMN nota_final TYPE DOUBLE PRECISION
    USING nota_final::DOUBLE PRECISION;
