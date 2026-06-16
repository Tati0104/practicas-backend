-- Alinear scope con rol (corrige admins/coordinadores con scope desactualizado)
UPDATE usuarios SET scope = 'GLOBAL' WHERE rol IN ('ADMIN', 'DIRECCION') AND scope <> 'GLOBAL';
UPDATE usuarios SET scope = 'FACULTAD' WHERE rol IN ('COORD_ACADEMICA', 'COORD_PRACTICA', 'SECRETARIA') AND scope <> 'FACULTAD';
UPDATE usuarios SET scope = 'PROGRAMA' WHERE rol IN ('DOCENTE_ASESOR', 'ESTUDIANTE') AND scope <> 'PROGRAMA';
UPDATE usuarios SET scope = 'ASIGNADO' WHERE rol IN ('EMPRESA', 'TUTOR_EMPRESARIAL') AND scope <> 'ASIGNADO';

-- Usuarios ESTUDIANTE creados solo en módulo usuarios: generar perfil académico mínimo
INSERT INTO estudiantes (
    usuario_id,
    identificacion,
    nombre,
    correo,
    programa_id,
    semestre,
    creditos_aprobados,
    promedio_acumulado,
    estado_aptitud
)
SELECT
    u.id,
    'USR-' || u.id,
    u.nombre,
    u.correo,
    (SELECT p.id FROM programas p WHERE p.activo = TRUE ORDER BY p.id LIMIT 1),
    1,
    0,
    0.00,
    'SIN_EVALUAR'
FROM usuarios u
WHERE u.rol = 'ESTUDIANTE'
  AND EXISTS (SELECT 1 FROM programas p WHERE p.activo = TRUE)
  AND NOT EXISTS (
      SELECT 1 FROM estudiantes e
      WHERE e.usuario_id = u.id OR LOWER(e.correo) = LOWER(u.correo)
  );

INSERT INTO expedientes (estudiante_id)
SELECT e.id
FROM estudiantes e
WHERE NOT EXISTS (SELECT 1 FROM expedientes ex WHERE ex.estudiante_id = e.id);
