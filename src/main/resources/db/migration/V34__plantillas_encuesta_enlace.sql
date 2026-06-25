-- RF-11-05: plantillas con enlace dinámico a encuestas
UPDATE plantillas_correo
SET cuerpo = 'Hola {{nombre}}, ya se encuentra disponible la encuesta de práctica. Accede aquí: {{enlace_encuesta}}'
WHERE codigo = 'ENCUESTA_DISPONIBLE';

UPDATE plantillas_correo
SET cuerpo = 'Hola {{nombre}}, recuerda completar la encuesta pendiente. Accede aquí: {{enlace_encuesta}}'
WHERE codigo = 'RECORDATORIO_ENCUESTA';
