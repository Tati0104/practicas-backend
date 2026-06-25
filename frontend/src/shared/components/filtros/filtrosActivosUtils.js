/**
 * Utilidades para el patrón de filtros activos (chips removibles).
 */

/** Claves de paginación que no son filtros de negocio. */
export const CLAVES_PAGINACION = ['page', 'size'];

export function valorActivo(valor) {
  return valor !== '' && valor !== null && valor !== undefined;
}

/** Etiqueta legible del valor según la definición del campo. */
export function etiquetaValor(campo, valor) {
  if (!campo) return String(valor);
  if (campo.type === 'select' && campo.opciones) {
    const op = campo.opciones.find((o) => String(o.value) === String(valor));
    if (op) return op.label;
  }
  if (campo.formatearValor) return campo.formatearValor(valor);
  return String(valor);
}

/** Convierte el objeto de filtros en chips visibles. */
export function chipsDesdeFiltros(filtros, campos) {
  return campos
    .filter((campo) => valorActivo(filtros[campo.key]))
    .map((campo) => ({
      key: campo.key,
      label: campo.label,
      valor: filtros[campo.key],
      texto: `${campo.label}: ${etiquetaValor(campo, filtros[campo.key])}`,
    }));
}

/** Limpia todos los campos configurados manteniendo paginación y extras. */
export function limpiarCamposFiltro(filtros, campos, extras = CLAVES_PAGINACION) {
  const vacios = Object.fromEntries(campos.map((c) => [c.key, '']));
  const conservar = Object.fromEntries(
    extras.filter((k) => filtros[k] !== undefined).map((k) => [k, filtros[k]])
  );
  return { ...filtros, ...vacios, ...conservar, page: 0 };
}
