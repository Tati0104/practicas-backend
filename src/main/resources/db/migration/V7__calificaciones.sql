-- Tabla: notas_docente
CREATE TABLE notas_docente (
    id BIGSERIAL PRIMARY KEY,
    instancia_practica_id BIGINT NOT NULL,
    nota NUMERIC(3, 2) NOT NULL,
    corte INT NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_notas_docente_instancias FOREIGN KEY (instancia_practica_id) REFERENCES instancias_practica(id)
);

-- Tabla: notas_tutor
CREATE TABLE notas_tutor (
    id BIGSERIAL PRIMARY KEY,
    instancia_practica_id BIGINT NOT NULL,
    nota NUMERIC(3, 2) NOT NULL,
    corte INT NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_notas_tutor_instancias FOREIGN KEY (instancia_practica_id) REFERENCES instancias_practica(id)
);

-- Tabla: notas_finales
CREATE TABLE notas_finales (
    id BIGSERIAL PRIMARY KEY,
    instancia_practica_id BIGINT UNIQUE NOT NULL,
    nota_final NUMERIC(3, 2) NOT NULL,
    aprobada BOOLEAN NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_notas_finales_instancias FOREIGN KEY (instancia_practica_id) REFERENCES instancias_practica(id)
);

-- Tabla: encuestas
CREATE TABLE encuestas (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    tipo VARCHAR(100) NOT NULL
);

-- Tabla: respuestas_encuesta
CREATE TABLE respuestas_encuesta (
    id BIGSERIAL PRIMARY KEY,
    encuesta_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    respuestas_json TEXT NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_respuestas_encuestas FOREIGN KEY (encuesta_id) REFERENCES encuestas(id),
    CONSTRAINT fk_respuestas_usuarios FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- Índices
CREATE INDEX idx_notas_docente_instancia ON notas_docente(instancia_practica_id);
CREATE INDEX idx_notas_tutor_instancia ON notas_tutor(instancia_practica_id);
CREATE INDEX idx_respuestas_encuesta ON respuestas_encuesta(encuesta_id);
CREATE INDEX idx_respuestas_usuario ON respuestas_encuesta(usuario_id);
