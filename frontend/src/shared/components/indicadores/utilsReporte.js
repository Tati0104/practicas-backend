import { PALETA, getPaleta } from './paletaIndicadores';

export function normalizarReporte(data) {
  if (!data) return null;
  return {
    totalVacantes: Number(data.totalVacantes) || 0,
    vacantesActivas: Number(data.vacantesActivas) || 0,
    vacantesPendientes: Number(data.vacantesPendientes) || 0,
    totalAsignaciones: Number(data.totalAsignaciones) || 0,
    asignacionesVinculadas: Number(data.asignacionesVinculadas) || 0,
    asignacionesCanceladas: Number(data.asignacionesCanceladas) || 0,
    estudiantesRegistrados: Number(data.estudiantesRegistrados) || 0,
  };
}

export function datosBarrasComparativa(r) {
  const otrasVacantes = Math.max(0, r.totalVacantes - r.vacantesActivas - r.vacantesPendientes);
  const otrasAsignaciones = Math.max(
    0,
    r.totalAsignaciones - r.asignacionesVinculadas - r.asignacionesCanceladas
  );

  return [
    {
      nombre: 'Vacantes',
      activas: r.vacantesActivas,
      pendientes: r.vacantesPendientes,
      otras: otrasVacantes,
    },
    {
      nombre: 'Asignaciones',
      activas: r.asignacionesVinculadas,
      pendientes: otrasAsignaciones,
      otras: r.asignacionesCanceladas,
    },
  ];
}

export function seriesBarrasVacantesAsignaciones(paleta = PALETA) {
  return [
    { key: 'activas', nombre: 'Activas / Vinculadas', color: paleta.primary },
    { key: 'pendientes', nombre: 'Pendientes / En proceso', color: paleta.accent },
    { key: 'otras', nombre: 'Otras / Canceladas', color: paleta.red },
  ];
}

export function datosAreaEvolucion(r) {
  return [
    { nombre: 'Vacantes', total: r.totalVacantes, activas: r.vacantesActivas },
    { nombre: 'Asignaciones', total: r.totalAsignaciones, activas: r.asignacionesVinculadas },
    { nombre: 'Estudiantes', total: r.estudiantesRegistrados, activas: r.estudiantesRegistrados },
  ];
}

export function seriesAreaComparativa(paleta = PALETA) {
  return [
    { key: 'total', nombre: 'Total registrado', color: paleta.primary },
    { key: 'activas', nombre: 'Activos / vinculados', color: paleta.emerald },
  ];
}

export function datosDonutAsignaciones(r, paleta = PALETA) {
  const enProceso = Math.max(
    0,
    r.totalAsignaciones - r.asignacionesVinculadas - r.asignacionesCanceladas
  );

  return [
    { nombre: 'Vinculadas', valor: r.asignacionesVinculadas, color: paleta.emerald },
    { nombre: 'En proceso', valor: enProceso, color: paleta.accent },
    { nombre: 'Canceladas', valor: r.asignacionesCanceladas, color: paleta.red },
  ];
}

export function datosDonutVacantes(r, paleta = PALETA) {
  const otras = Math.max(0, r.totalVacantes - r.vacantesActivas - r.vacantesPendientes);
  return [
    { nombre: 'Activas', valor: r.vacantesActivas, color: paleta.primary },
    { nombre: 'Pendientes', valor: r.vacantesPendientes, color: paleta.accent },
    { nombre: 'Otras', valor: otras, color: paleta.slate },
  ];
}

export { getPaleta };
