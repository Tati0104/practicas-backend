import { useQuery } from '@tanstack/react-query';
import http from '@/shared/services/http';

/**
 * @typedef {Object} NotaCorte
 * @property {number} corte
 * @property {number|null} notaDocente
 * @property {string|null} obsDocente
 * @property {number|null} notaTutor
 * @property {string|null} obsTutor
 */

/**
 * @typedef {Object} ResumenCalificaciones
 * @property {number} practicaId
 * @property {NotaCorte[]} notasCortes
 * @property {number|null} notaFinal
 * @property {boolean|null} aprobada
 * @property {number} promedioEstimado
 */

export default function useCalificaciones(practicaId, practicaActiva = true) {
  return useQuery({
    queryKey: ['calificaciones', practicaId],
    queryFn: async () => {
      const { data } = await http.get(`/calificaciones/${practicaId}/resumen`);
      return data;
    },
    enabled: Boolean(practicaId) && practicaActiva !== false,
  });
}
