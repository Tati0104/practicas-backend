-- KBM - Verificación documental de ENUMs base del sistema.
-- Este script no modifica estructura: documenta valores válidos para evitar inconsistencias.

-- estado_asignacion: ASIGNADA, EN_PROCESO_VINCULACION, VINCULADA, CANCELADA
-- estado_vacante según V5 de EHS: PENDIENTE_APROBACION, ACTIVA, PAUSADA, CUPOS_COMPLETOS, CERRADA
-- KBM agrega RECHAZADA en V10 porque PE-26/PE-27 exige rechazo de vacantes.
-- estado_aptitud: SIN_EVALUAR, APTO, NO_APTO
-- tipo_evento_correo: NUEVA_ASIGNACION, CAMBIO_ESTADO, ENCUESTA_DISPONIBLE, RECORDATORIO_ENCUESTA, ALERTA_INACTIVIDAD, CONFIRMACION_VINCULACION, RESULTADO_CIERRE
