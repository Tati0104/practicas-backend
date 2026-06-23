export const ESTADO_PRACTICA = {
  EN_CURSO: { label: 'En curso', variant: 'success' },
  COMPLETADA: { label: 'Finalizada', variant: 'neutral' },
  REPROBADA: { label: 'Reprobada', variant: 'danger' },
  ASIGNADA_PENDIENTE_INICIO: { label: 'Pendiente de inicio', variant: 'warning' },
  CANCELADA: { label: 'Cancelada', variant: 'neutral' },
};

export const OPCIONES_ESTADO_PRACTICA = [
  { value: 'EN_CURSO', label: 'En curso' },
  { value: 'COMPLETADA', label: 'Finalizada' },
  { value: 'REPROBADA', label: 'Reprobada' },
  { value: 'ASIGNADA_PENDIENTE_INICIO', label: 'Pendiente de inicio' },
];

export const ROLES_EXPEDIENTE = ['COORD_PRACTICA', 'ADMIN', 'SECRETARIA', 'COORD_ACADEMICA'];

export function badgeEstadoPractica(estadoPractica) {
  return (
    ESTADO_PRACTICA[estadoPractica] ?? {
      label: estadoPractica?.replace(/_/g, ' ') ?? 'Sin estado',
      variant: 'neutral',
    }
  );
}

export function practicaFinalizada(estadoPractica) {
  return estadoPractica === 'COMPLETADA' || estadoPractica === 'REPROBADA';
}
