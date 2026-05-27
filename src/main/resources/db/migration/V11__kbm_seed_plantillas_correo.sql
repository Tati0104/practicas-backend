-- KBM - Plantillas base para PE-nuevo PlantillaCorreoService.
-- La tabla plantillas_correo fue creada por EHS en V1 con codigo, asunto y cuerpo.

INSERT INTO plantillas_correo (codigo, asunto, cuerpo)
VALUES
('NUEVA_ASIGNACION', 'Nueva asignación de práctica', 'Hola {{nombre_estudiante}}, tienes una nueva asignación con la empresa {{empresa}}.'),
('CAMBIO_ESTADO', 'Cambio de estado en el sistema', 'Hola {{nombre}}, el estado cambió a {{estado}}.'),
('ENCUESTA_DISPONIBLE', 'Encuesta disponible', 'Hola {{nombre}}, ya se encuentra disponible la encuesta de práctica.'),
('RECORDATORIO_ENCUESTA', 'Recordatorio de encuesta pendiente', 'Hola {{nombre}}, recuerda completar la encuesta pendiente.'),
('ALERTA_INACTIVIDAD', 'Alerta de inactividad', 'Se detectó inactividad en la práctica de {{nombre_estudiante}}.'),
('CONFIRMACION_VINCULACION', 'Confirmación de vinculación', 'Hola {{nombre_estudiante}}, tu vinculación con {{empresa}} fue confirmada.'),
('RESULTADO_CIERRE', 'Resultado de cierre de práctica', 'Hola {{nombre_estudiante}}, el resultado de cierre de tu práctica es {{resultado}}.')
ON CONFLICT (codigo) DO NOTHING;
