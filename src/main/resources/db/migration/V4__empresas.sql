-- Tabla: empresas
CREATE TABLE empresas (
    id BIGSERIAL PRIMARY KEY,
    nit VARCHAR(50) UNIQUE NOT NULL,
    razon_social VARCHAR(255) NOT NULL,
    sector_id BIGINT NOT NULL,
    direccion VARCHAR(255),
    municipio VARCHAR(150),
    telefono VARCHAR(50),
    activo BOOLEAN DEFAULT TRUE NOT NULL,
    CONSTRAINT fk_empresas_sector FOREIGN KEY (sector_id) REFERENCES catalogo_items(id)
);

-- Tabla: tutores_empresariales
CREATE TABLE tutores_empresariales (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    cargo VARCHAR(100),
    correo VARCHAR(150) UNIQUE NOT NULL,
    telefono VARCHAR(50),
    usuario_id BIGINT UNIQUE,
    activo BOOLEAN DEFAULT TRUE NOT NULL,
    CONSTRAINT fk_tutores_empresas FOREIGN KEY (empresa_id) REFERENCES empresas(id),
    CONSTRAINT fk_tutores_usuarios FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- Agregar llaves foráneas a instancias_practica que dependen de empresas y tutores
ALTER TABLE instancias_practica 
    ADD CONSTRAINT fk_instancias_empresas FOREIGN KEY (empresa_id) REFERENCES empresas(id);

ALTER TABLE instancias_practica 
    ADD CONSTRAINT fk_instancias_tutores FOREIGN KEY (tutor_id) REFERENCES tutores_empresariales(id);

-- Índices
CREATE INDEX idx_empresas_sector ON empresas(sector_id);
CREATE INDEX idx_tutores_empresa ON tutores_empresariales(empresa_id);
CREATE INDEX idx_tutores_usuario ON tutores_empresariales(usuario_id);
CREATE INDEX idx_instancias_empresa ON instancias_practica(empresa_id);
CREATE INDEX idx_instancias_tutor ON instancias_practica(tutor_id);
