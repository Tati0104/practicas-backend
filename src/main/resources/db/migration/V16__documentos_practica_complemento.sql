-- Complemento PE-34: categorías, estado de carga y usuario que cargó el documento
ALTER TABLE documentos_practica
    ADD COLUMN categoria VARCHAR(50),
    ADD COLUMN estado VARCHAR(20) DEFAULT 'PENDIENTE' NOT NULL,
    ADD COLUMN usuario_carga_id BIGINT;

ALTER TABLE documentos_practica
    ADD CONSTRAINT fk_documentos_usuario_carga
        FOREIGN KEY (usuario_carga_id) REFERENCES usuarios(id);

UPDATE documentos_practica
SET categoria = tipo
WHERE categoria IS NULL
  AND tipo IN ('VINCULACION', 'SEGUIMIENTO', 'EVALUACIONES_ENCUESTAS', 'ACTA_CIERRE');

UPDATE documentos_practica
SET estado = 'CARGADO'
WHERE url IS NOT NULL AND TRIM(url) <> '';

UPDATE documentos_practica
SET estado = 'PENDIENTE'
WHERE estado IS NULL OR url IS NULL OR TRIM(url) = '';

ALTER TABLE documentos_practica
    ADD CONSTRAINT chk_documento_categoria
        CHECK (categoria IN ('VINCULACION', 'SEGUIMIENTO', 'EVALUACIONES_ENCUESTAS', 'ACTA_CIERRE'));

ALTER TABLE documentos_practica
    ADD CONSTRAINT chk_documento_estado
        CHECK (estado IN ('CARGADO', 'PENDIENTE'));

CREATE INDEX idx_documentos_categoria ON documentos_practica(categoria);
CREATE INDEX idx_documentos_estado ON documentos_practica(estado);
