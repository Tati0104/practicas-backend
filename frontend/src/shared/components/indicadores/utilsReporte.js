import { PALETA } from './paletaIndicadores';

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

export function seriesBarrasVacantesAsignaciones() {
  return [
    { key: 'activas', nombre: 'Activas / Vinculadas', color: PALETA.primary },
    { key: 'pendientes', nombre: 'Pendientes / En proceso', color: PALETA.accent },
    { key: 'otras', nombre: 'Otras / Canceladas', color: PALETA.red },
  ];
}

export function datosAreaEvolucion(r) {
  return [
    { nombre: 'Vacantes', total: r.totalVacantes, activas: r.vacantesActivas },
    { nombre: 'Asignaciones', total: r.totalAsignaciones, activas: r.asignacionesVinculadas },
    { nombre: 'Estudiantes', total: r.estudiantesRegistrados, activas: r.estudiantesRegistrados },
  ];
}

export function seriesAreaComparativa() {
  return [
    { key: 'total', nombre: 'Total registrado', color: PALETA.primary },
    { key: 'activas', nombre: 'Activos / vinculados', color: PALETA.emerald },
  ];
}

export function datosDonutAsignaciones(r) {
  const enProceso = Math.max(
    0,
    r.totalAsignaciones - r.asignacionesVinculadas - r.asignacionesCanceladas
  );

  return [
    { nombre: 'Vinculadas', valor: r.asignacionesVinculadas, color: PALETA.emerald },
    { nombre: 'En proceso', valor: enProceso, color: PALETA.accent },
    { nombre: 'Canceladas', valor: r.asignacionesCanceladas, color: PALETA.red },
  ];
}

export function datosDonutVacantes(r) {
  const otras = Math.max(0, r.totalVacantes - r.vacantesActivas - r.vacantesPendientes);
  return [
    { nombre: 'Activas', valor: r.vacantesActivas, color: PALETA.primary },
    { nombre: 'Pendientes', valor: r.vacantesPendientes, color: PALETA.accent },
    { nombre: 'Otras', valor: otras, color: PALETA.slate },
  ];
}
