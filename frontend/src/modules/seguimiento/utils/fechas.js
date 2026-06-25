export function parseFechaSeguimiento(fecha) {
  if (!fecha) return null;

  if (Array.isArray(fecha)) {
    const [y, m, d, h = 0, min = 0, s = 0] = fecha;
    return new Date(y, m - 1, d, h, min, s);
  }

  const parsed = new Date(fecha);
  return Number.isNaN(parsed.getTime()) ? null : parsed;
}

export function formatearFechaSeguimiento(fecha, options) {
  const parsed = parseFechaSeguimiento(fecha);
  if (!parsed) return '—';
  return parsed.toLocaleDateString('es-CO', options);
}

export function formatearFechaHoraSeguimiento(fecha) {
  const parsed = parseFechaSeguimiento(fecha);
  if (!parsed) return '—';
  return parsed.toLocaleString('es-CO', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export function nombreEstudiantePractica(practica) {
  if (!practica?.estudiante) return '—';
  if (typeof practica.estudiante === 'string') return practica.estudiante;
  return practica.estudiante.nombre ?? '—';
}

export function estadoSeguimientoPractica(practica) {
  return practica?.estadoSeguimiento ?? practica?.estado ?? '';
}
