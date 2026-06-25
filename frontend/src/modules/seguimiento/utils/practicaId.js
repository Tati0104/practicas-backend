/**
 * Resuelve el identificador numérico de una práctica desde distintas formas
 * (objeto del tablero, string de la URL, número directo).
 * @param {unknown} valor
 * @returns {number|null}
 */
export function resolverIdPractica(valor) {
  if (valor == null || valor === '') return null;

  if (typeof valor === 'object') {
    const candidatos = [valor.id, valor.practicaId, valor.instanciaPracticaId];
    for (const candidato of candidatos) {
      const id = resolverIdPractica(candidato);
      if (id != null) return id;
    }
    return null;
  }

  const texto = String(valor).trim();
  if (!texto || texto === '[object Object]' || texto === 'undefined' || texto === 'null') {
    return null;
  }

  const numero = Number(texto);
  return Number.isFinite(numero) && numero > 0 ? numero : null;
}
