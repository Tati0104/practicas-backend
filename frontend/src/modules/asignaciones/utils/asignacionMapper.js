/**
 * Adapta la respuesta del API de asignaciones al shape que usa la UI.
 */
export function normalizarAsignacion(raw) {
  if (!raw) return null;

  const base = raw.asignacion ?? raw;
  const estudianteApi = base.estudiante;
  const vacanteApi = base.vacante;

  const estudiante = estudianteApi
    ? {
        nombre: estudianteApi.nombre,
        codigo: estudianteApi.identificacion ?? estudianteApi.codigo,
        programa: estudianteApi.programa,
      }
    : null;

  const vacante = vacanteApi
    ? {
        cargo: vacanteApi.cargo,
        empresa: vacanteApi.empresaNombre ?? vacanteApi.empresa,
        modalidad: vacanteApi.modalidad,
      }
    : null;

  return {
    ...base,
    estudiante,
    vacante,
    fechaAsignacion: base.fechaAsignacion ?? base.fechaCreacion ?? null,
    historial: raw.historial ?? base.historial ?? [],
  };
}

export function normalizarPaginaAsignaciones(pagina) {
  if (!pagina) return pagina;
  if (!Array.isArray(pagina.content)) return pagina;
  return {
    ...pagina,
    content: pagina.content.map(normalizarAsignacion),
  };
}
