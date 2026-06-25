import { Badge } from '@/shared/components/ui';

const VARIANTES = {
  ACTIVA: 'success',
  PENDIENTE_APROBACION: 'warning',
  PAUSADA: 'neutral',
  CUPOS_COMPLETOS: 'warning',
  CERRADA: 'danger',
};

export function etiquetaEstadoVacante(estado) {
  return estado?.replace(/_/g, ' ') ?? '—';
}

export default function BadgeVacanteEstado({ estado }) {
  return (
    <Badge variant={VARIANTES[estado] ?? 'neutral'}>
      {etiquetaEstadoVacante(estado)}
    </Badge>
  );
}
