-- Usuario administrador inicial para pruebas de desarrollo/Postman.
-- Credenciales: admin@test.com / Admin12345
INSERT INTO usuarios (
    nombre,
    correo,
    password_hash,
    rol,
    scope,
    activo,
    primera_vez,
    created_at,
    updated_at
) VALUES (
    'Admin Prueba',
    'admin@test.com',
    '$2y$10$/IaMV6MU4eZI3u8PNvSba.x8m1SvDSbozRGIEvdiI5yjdVcsDpPpu',
    'ADMIN',
    'GLOBAL',
    TRUE,
    FALSE,
    NOW(),
    NOW()
) ON CONFLICT (correo) DO NOTHING;
