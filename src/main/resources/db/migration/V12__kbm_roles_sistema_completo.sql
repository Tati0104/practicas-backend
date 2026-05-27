-- KBM - Alineación de roles con planificación S0/S1.
-- EHS V1 creó usuarios.rol con un CHECK inicial reducido. Se amplía para soportar los 9 roles AVH.

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_usuarios_rol'
    ) THEN
        ALTER TABLE usuarios DROP CONSTRAINT chk_usuarios_rol;
    END IF;
END $$;

ALTER TABLE usuarios
    ADD CONSTRAINT chk_usuarios_rol
    CHECK (rol IN ('ADMIN', 'DIRECCION', 'COORD_ACADEMICA', 'COORD_PRACTICA', 'SECRETARIA', 'DOCENTE_ASESOR', 'EMPRESA', 'TUTOR_EMPRESARIAL', 'ESTUDIANTE'));
