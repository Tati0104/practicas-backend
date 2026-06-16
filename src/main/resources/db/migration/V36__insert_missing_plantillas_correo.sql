INSERT INTO plantillas_correo (codigo, asunto, cuerpo)
VALUES
('VACANTE_CREADA', 'Nueva vacante registrada', 'Hola, se ha registrado la nueva vacante {{cargo}} en la empresa {{empresa}} y está pendiente de aprobación.'),
('VACANTE_APROBADA', 'Vacante aprobada', 'Hola, la vacante {{cargo}} en la empresa {{empresa}} ha sido aprobada exitosamente.'),
('VACANTE_RECHAZADA', 'Vacante rechazada', 'Hola, la vacante {{cargo}} en la empresa {{empresa}} ha sido rechazada.'),
('DOCENTE_ASESOR_CREADO', 'Bienvenido(a) como Docente Asesor', 'Hola {{nombre}}, has sido registrado(a) como Docente Asesor en el sistema. Tu correo de acceso es {{correo}}.')
ON CONFLICT (codigo) DO NOTHING;
