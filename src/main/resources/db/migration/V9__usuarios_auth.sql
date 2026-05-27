-- Ampliación de tabla usuarios para módulo auth (PE-12)

ALTER TABLE usuarios RENAME COLUMN password TO password_hash;

ALTER TABLE usuarios
    ADD COLUMN nombre VARCHAR(150),
    ADD COLUMN scope VARCHAR(50) DEFAULT 'GLOBAL' NOT NULL,
    ADD COLUMN primera_vez BOOLEAN DEFAULT TRUE NOT NULL,
    ADD COLUMN token_recuperacion VARCHAR(255),
    ADD COLUMN token_expiracion TIMESTAMP,
    ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL;

UPDATE usuarios
SET nombre = SPLIT_PART(correo, '@', 1)
WHERE nombre IS NULL;

ALTER TABLE usuarios ALTER COLUMN nombre SET NOT NULL;

ALTER TABLE usuarios DROP CONSTRAINT IF EXISTS chk_usuarios_rol;

ALTER TABLE usuarios ADD CONSTRAINT chk_usuarios_rol CHECK (
    rol IN (
        'ADMIN',
        'DIRECCION',
        'COORD_ACADEMICA',
        'COORD_PRACTICA',
        'SECRETARIA',
        'DOCENTE_ASESOR',
        'EMPRESA',
        'TUTOR_EMPRESARIAL',
        'ESTUDIANTE'
    )
);

ALTER TABLE usuarios ADD CONSTRAINT chk_usuarios_scope CHECK (
    scope IN ('GLOBAL', 'FACULTAD', 'PROGRAMA', 'ASIGNADO')
);

CREATE INDEX idx_usuarios_correo ON usuarios(correo);
CREATE INDEX idx_usuarios_token_recuperacion ON usuarios(token_recuperacion);
CREATE INDEX idx_usuarios_rol_activo ON usuarios(rol, activo);
