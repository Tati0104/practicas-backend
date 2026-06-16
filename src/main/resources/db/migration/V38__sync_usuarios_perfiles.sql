-- Vincular perfiles existentes con usuarios por correo y alinear nombres
UPDATE docentes_asesores d
SET nombre = u.nombre,
    usuario_id = u.id
FROM usuarios u
WHERE LOWER(d.correo) = LOWER(u.correo)
  AND u.rol = 'DOCENTE_ASESOR'
  AND (d.usuario_id IS NULL OR d.usuario_id <> u.id OR d.nombre <> u.nombre);

UPDATE tutores_empresariales t
SET nombre = u.nombre,
    usuario_id = u.id
FROM usuarios u
WHERE LOWER(t.correo) = LOWER(u.correo)
  AND u.rol = 'TUTOR_EMPRESARIAL'
  AND (t.usuario_id IS NULL OR t.usuario_id <> u.id OR t.nombre <> u.nombre);

UPDATE estudiantes e
SET nombre = u.nombre,
    usuario_id = u.id
FROM usuarios u
WHERE LOWER(e.correo) = LOWER(u.correo)
  AND u.rol = 'ESTUDIANTE'
  AND (e.usuario_id IS NULL OR e.usuario_id <> u.id OR e.nombre <> u.nombre);

UPDATE usuarios u
SET correo = LOWER(TRIM(u.correo))
WHERE u.correo <> LOWER(TRIM(u.correo));

UPDATE docentes_asesores d
SET correo = LOWER(TRIM(d.correo))
WHERE d.correo <> LOWER(TRIM(d.correo));

UPDATE tutores_empresariales t
SET correo = LOWER(TRIM(t.correo))
WHERE t.correo <> LOWER(TRIM(t.correo));

UPDATE estudiantes e
SET correo = LOWER(TRIM(e.correo))
WHERE e.correo <> LOWER(TRIM(e.correo));
