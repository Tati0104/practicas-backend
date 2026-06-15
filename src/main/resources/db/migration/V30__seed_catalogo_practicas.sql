-- Seed inicial de catálogo de prácticas (práctica 1) para todos los programas activos.
-- Necesario para que marcarApto pueda crear la InstanciaPractica correspondiente.
-- Usa ON CONFLICT DO NOTHING para ser idempotente si se vuelve a ejecutar.
INSERT INTO catalogo_practicas
    (programa_id, numero_practica, nombre, materia_nucleo, codigo_materia, num_cortes, duracion_semanas, activo)
SELECT
    p.id,
    1,
    'Práctica Empresarial I',
    'Práctica Empresarial',
    'PE-101',
    3,
    16,
    true
FROM programas p
WHERE p.activo = true
ON CONFLICT (programa_id, numero_practica) DO NOTHING;
