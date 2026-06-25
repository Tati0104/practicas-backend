import { Badge } from '@/shared/components/ui';

const VARIANTES = {
  ASIGNADA: 'info',
  EN_PROCESO_VINCULACION: 'warning',
  VINCULADA: 'success',
  CANCELADA: 'danger',
};

const ETIQUETAS = {
  ASIGNADA: 'Asignada',
  EN_PROCESO_VINCULACION: 'En Vinculación',
  VINCULADA: 'Vinculada',
  CANCELADA: 'Cancelada',
};

export default function BadgeAsignacion({ estado }) {
  return (
    <Badge variant={VARIANTES[estado] ?? 'neutral'} className="whitespace-nowrap">
      {ETIQUETAS[estado] || estado}
    </Badge>
  );
}
