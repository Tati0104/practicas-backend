import { useQuery } from '@tanstack/react-query';
import http from '@/shared/services/http';

/**
 * @typedef {Object} ItemChecklist
 * @property {string} nombre
 * @property {boolean} obligatorio
 * @property {boolean} verificado
 * @property {'PENDIENTE'|'COMPLETADO'|'EN_BORRADOR'|'COMPLETADA'} estado
 * @property {'TUTOR'|'ESTUDIANTE'|null} [tipoEncuesta]
 * @property {string|null} [estadoEncuesta]
 * @property {string|null} [fechaUltimoRecordatorio]
 * @property {string|null} [id]
 * @property {string|null} [tipo]
 * @property {string|null} [grupoId]
 * @property {ItemChecklist[]} [hijos]
 * @property {ItemChecklist[]} [items]
 */

/**
 * @typedef {Object} GrupoChecklist
 * @property {string} nombre
 * @property {number} progreso
 * @property {boolean} verificado
 * @property {'PENDIENTE'|'COMPLETADO'|'EN_BORRADOR'} estado
 * @property {ItemChecklist[]} items
 */

/**
 * @typedef {Object} ResumenChecklist
 * @property {number} practicaId
 * @property {number} progresoGlobal
 * @property {boolean} habilitarBotonCierre
 * @property {number} totalItems
 * @property {number} itemsCompletados
 * @property {number} itemsPendientes
 * @property {GrupoChecklist[]} grupos
 */

/**
 * Normaliza la respuesta del checklist a la estructura de grupos del backend.
 * Soporta también un arreglo plano con grupoId para renderizado recursivo.
 *
 * @param {ResumenChecklist|ItemChecklist[]|null|undefined} data
 * @returns {GrupoChecklist[]}
 */
export function normalizarGruposChecklist(data) {
  if (!data) return [];

  if (Array.isArray(data.grupos) && data.grupos.length > 0) {
    return data.grupos;
  }

  if (Array.isArray(data)) {
    return construirGruposDesdeItemsPlanos(data);
  }

  return [];
}

/**
 * @param {ItemChecklist[]} items
 * @returns {GrupoChecklist[]}
 */
function construirGruposDesdeItemsPlanos(items) {
  const gruposMap = new Map();

  items.forEach((item) => {
    if (item.grupoId == null && Array.isArray(item.hijos) && item.hijos.length > 0) {
      gruposMap.set(item.id ?? item.nombre, {
        nombre: item.nombre,
        progreso: calcularProgresoGrupo(item.hijos),
        verificado: item.hijos.every((hijo) => esItemCompletado(hijo)),
        estado: item.estado ?? 'PENDIENTE',
        items: item.hijos,
      });
      return;
    }

    const grupoId = item.grupoId ?? 'general';
    if (!gruposMap.has(grupoId)) {
      gruposMap.set(grupoId, {
        nombre: String(grupoId),
        progreso: 0,
        verificado: false,
        estado: 'PENDIENTE',
        items: [],
      });
    }
    gruposMap.get(grupoId).items.push(item);
  });

  return Array.from(gruposMap.values()).map((grupo) => ({
    ...grupo,
    progreso: calcularProgresoGrupo(grupo.items),
    verificado: grupo.items.every((item) => esItemCompletado(item)),
  }));
}

/**
 * @param {ItemChecklist[]} items
 */
function calcularProgresoGrupo(items) {
  if (!items?.length) return 0;
  const completados = items.filter((item) => esItemCompletado(item)).length;
  return completados / items.length;
}

/**
 * @param {ItemChecklist} item
 */
export function esItemCompletado(item) {
  if (!item) return false;
  if (item.verificado === true) return true;
  const estado = item.estado;
  return estado === 'COMPLETADO' || estado === 'COMPLETADA';
}

/**
 * @param {ItemChecklist} item
 */
export function itemPendienteRecordatorio(item) {
  if (!item || esItemCompletado(item)) return false;
  return item.estado === 'PENDIENTE' || item.estado === 'EN_BORRADOR';
}

export default function useCierre(practicaId) {
  return useQuery({
    queryKey: ['cierre', practicaId],
    queryFn: async () => {
      const { data: response } = await http.get(`/cierres/${practicaId}/checklist`);
      return response?.data ?? response;
    },
    enabled: Boolean(practicaId),
  });
}
