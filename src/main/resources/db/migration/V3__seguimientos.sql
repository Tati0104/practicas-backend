-- Tabla: observaciones_docente
CREATE TABLE observaciones_docente (
    id BIGSERIAL PRIMARY KEY,
    instancia_practica_id BIGINT NOT NULL,
    observacion TEXT NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_observaciones_instancias FOREIGN KEY (instancia_practica_id) REFERENCES instancias_practica(id)
);

-- Tabla: avances_tutor
CREATE TABLE avances_tutor (
    id BIGSERIAL PRIMARY KEY,
    instancia_practica_id BIGINT NOT NULL,
    avance TEXT NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_avances_instancias FOREIGN KEY (instancia_practica_id) REFERENCES instancias_practica(id)
);

-- Tabla: bitacora_estudiante
CREATE TABLE bitacora_estudiante (
    id BIGSERIAL PRIMARY KEY,
    instancia_practica_id BIGINT NOT NULL,
    descripcion TEXT NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_bitacora_instancias FOREIGN KEY (instancia_practica_id) REFERENCES instancias_practica(id)
);

-- Tabla: alertas_sistema
CREATE TABLE alertas_sistema (
    id BIGSERIAL PRIMARY KEY,
    mensaje TEXT NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    leida BOOLEAN DEFAULT FALSE NOT NULL
);

-- Tabla: bitacora_auditoria
CREATE TABLE bitacora_auditoria (
    id BIGSERIAL PRIMARY KEY,
    tabla_afectada VARCHAR(100) NOT NULL,
    accion VARCHAR(50) NOT NULL,
    usuario_id BIGINT,
    detalle TEXT,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_auditoria_usuarios FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- Índices
CREATE INDEX idx_observaciones_instancia ON observaciones_docente(instancia_practica_id);
CREATE INDEX idx_avances_instancia ON avances_tutor(instancia_practica_id);
CREATE INDEX idx_bitacora_instancia ON bitacora_estudiante(instancia_practica_id);
CREATE INDEX idx_auditoria_usuario ON bitacora_auditoria(usuario_id);
