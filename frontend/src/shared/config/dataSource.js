import { keepPreviousData } from '@tanstack/react-query';
import { normalizarPagina } from '../utils/paginacion';

/**
 * Fuente de datos del frontend.
 *
 * VITE_USE_MOCKS=true  → solo datos mock (sin llamadas HTTP de lectura).
 * VITE_USE_MOCKS=false → solo backend real (sin mezclar mocks en errores).
 *
 * Cambia el valor en frontend/.env y reinicia `npm run dev`.
 */
export const USE_MOCKS = import.meta.env.VITE_USE_MOCKS === 'true';

export function usarMocks() {
  return USE_MOCKS;
}

export function etiquetaFuenteDatos() {
  return USE_MOCKS ? 'MOCK' : 'API';
}

/**
 * Ejecuta mock o API según VITE_USE_MOCKS. Nunca mezcla ambos en la misma consulta.
 * @template T
 * @param {{ mock: () => T | Promise<T>, api: () => T | Promise<T> }} fuentes
 * @returns {Promise<T>}
 */
export async function ejecutarConsulta({ mock, api }) {
  if (USE_MOCKS) {
    return mock();
  }
  return api();
}

/**
 * Placeholder de TanStack Query: mock solo en modo MOCK; keepPreviousData en modo API.
 * @param {unknown} mockData
 */
export function placeholderDesdeMock(mockData) {
  if (!USE_MOCKS) {
    return keepPreviousData;
  }
  return (previousData) => previousData ?? normalizarPagina(mockData);
}

/**
 * Placeholder simple (listas no paginadas).
 * @param {unknown} mockData
 */
export function placeholderSimple(mockData) {
  if (!USE_MOCKS) {
    return undefined;
  }
  return mockData;
}

/**
 * Pagina un arreglo en cliente (útil cuando el backend devuelve List sin Page).
 * @param {unknown[]} items
 * @param {{ page?: number, size?: number }} filtros
 */
export function paginarEnCliente(items, filtros = {}) {
  const page = filtros.page ?? 0;
  const size = filtros.size ?? 10;
  const inicio = page * size;

  return {
    content: items.slice(inicio, inicio + size),
    totalElements: items.length,
    totalPages: Math.max(1, Math.ceil(items.length / size) || 1),
  };
}
