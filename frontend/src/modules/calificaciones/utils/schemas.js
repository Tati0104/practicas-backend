import { z } from 'zod';

export const NOTA_MINIMA_APROBACION = 3.0;
export const CORTE_REFERENCIA = 1;

export const notaSchema = z.object({
  nota: z
    .number({ invalid_type_error: 'Ingresa una nota válida' })
    .min(0, 'La nota mínima es 0.0')
    .max(5, 'La nota máxima es 5.0')
    .refine((valor) => Math.round(valor * 10) === valor * 10, {
      message: 'Usa máximo un decimal (ej: 4.5)',
    }),
  observaciones: z.string().max(500, 'Máximo 500 caracteres').optional().or(z.literal('')),
});

export const notaFinalSchema = z.object({
  nota: z
    .number({ invalid_type_error: 'Ingresa una nota válida' })
    .min(0, 'La nota mínima es 0.0')
    .max(5, 'La nota máxima es 5.0')
    .refine((valor) => Math.round(valor * 10) === valor * 10, {
      message: 'Usa máximo un decimal (ej: 4.5)',
    }),
});

/**
 * @param {import('axios').AxiosError} error
 */
export function extraerMensajeError(error, fallback = 'Error inesperado. Intenta de nuevo.') {
  const data = error?.response?.data;
  if (!data) return fallback;
  if (typeof data === 'string') return data;
  return data.mensaje ?? data.message ?? fallback;
}

/**
 * @param {import('../hooks/useCalificaciones').ResumenCalificaciones|null|undefined} resumen
 */
export function obtenerNotasReferencia(resumen) {
  if (!resumen?.notasCortes?.length) {
    return { corte: CORTE_REFERENCIA, notaDocente: null, obsDocente: null, notaTutor: null, obsTutor: null };
  }

  const corteData =
    resumen.notasCortes.find((item) => item.corte === CORTE_REFERENCIA) ?? resumen.notasCortes[0];

  return {
    corte: corteData.corte ?? CORTE_REFERENCIA,
    notaDocente: corteData.notaDocente ?? null,
    obsDocente: corteData.obsDocente ?? null,
    notaTutor: corteData.notaTutor ?? null,
    obsTutor: corteData.obsTutor ?? null,
  };
}

/**
 * @param {import('../hooks/useCalificaciones').ResumenCalificaciones|null|undefined} resumen
 */
export function obtenerResultadoEstimado(resumen) {
  if (!resumen) return null;

  if (resumen.notaFinal != null && resumen.aprobada != null) {
    return resumen.aprobada ? 'APROBADA' : 'REPROBADA';
  }

  const promedio = resumen.promedioEstimado ?? 0;
  if (promedio <= 0) return null;

  return promedio >= NOTA_MINIMA_APROBACION ? 'APROBADA' : 'REPROBADA';
}
