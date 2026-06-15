import { Badge } from '@/shared/components/ui';

const VARIANTES = {
  PENDIENTE: 'neutral',
  SUBIDO: 'info',
  FIRMADO: 'success',
};

const ETIQUETAS = {
  PENDIENTE: 'Pendiente',
  SUBIDO: 'Subido',
  FIRMADO: 'Firmado',
};

export default function BadgeDocumento({ estado }) {
  return (
    <Badge variant={VARIANTES[estado] ?? 'neutral'} className="whitespace-nowrap">
      {ETIQUETAS[estado] ?? 'Pendiente'}
    </Badge>
  );
}
