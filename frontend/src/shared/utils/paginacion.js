import { placeholderDesdeMock } from '../config/dataSource';

export const PAGINA_INICIAL = 0;
export const TAMANO_PAGINA_DEFAULT = 10;

/**
 * Extrae page/size del objeto de filtros sin perder el resto de parámetros.
 * @param {Record<string, unknown>} [filtros]
 */
export function paramsListado(filtros = {}) {
  const { page = PAGINA_INICIAL, size = TAMANO_PAGINA_DEFAULT, ...rest } = filtros;
  return { ...rest, page, size };
}

/**
 * Normaliza respuestas paginadas del backend o arreglos planos.
 * @param {unknown} data
 * @param {unknown[]} [fallbackContent]
 */
export function normalizarPagina(data, fallbackContent = []) {
  if (!data) {
    return {
      content: fallbackContent,
      totalElements: fallbackContent.length,
      totalPages: 1,
    };
  }

  if (Array.isArray(data)) {
    return {
      content: data,
      totalElements: data.length,
      totalPages: 1,
    };
  }

  const content = data.content ?? fallbackContent;

  return {
    content,
    totalElements: data.totalElements ?? content.length,
    totalPages: data.totalPages ?? 1,
  };
}

/**
 * @deprecated Usar placeholderDesdeMock de shared/config/dataSource.js
 */
export function placeholderPaginado(mockData) {
  return placeholderDesdeMock(mockData);
}
