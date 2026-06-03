-- Migración para Sprint 5: Reestructurar encuestas para almacenar estado, tipo y respuestas directamente

DROP TABLE IF EXISTS respuestas_encuesta;
DROP TABLE IF EXISTS encuestas;

CREATE TABLE encuestas (
    id BIGSERIAL PRIMARY KEY,
    instancia_practica_id BIGINT NOT NULL,
    tipo VARCHAR(50) NOT NULL, -- TUTOR, ESTUDIANTE
    estado VARCHAR(50) NOT NULL, -- PENDIENTE, EN_BORRADOR, COMPLETADA
    fecha_envio_invitacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    respuestas_json TEXT,
    CONSTRAINT fk_encuestas_instancia FOREIGN KEY (instancia_practica_id) REFERENCES instancias_practica(id),
    CONSTRAINT chk_encuesta_tipo CHECK (tipo IN ('TUTOR', 'ESTUDIANTE')),
    CONSTRAINT chk_encuesta_estado CHECK (estado IN ('PENDIENTE', 'EN_BORRADOR', 'COMPLETADA'))
);

CREATE INDEX idx_encuestas_instancia ON encuestas(instancia_practica_id);
