// src/modules/seguimiento/components/PracticaCard.jsx

import { Badge, Button, Card } from '@/shared/components/ui';

const BADGE = {
  AL_DIA: { label: 'Al día', variant: 'success' },
  PENDIENTE: { label: 'Pendiente', variant: 'warning' },
  EN_ALERTA: { label: 'En alerta', variant: 'danger' },
};

export default function PracticaCard({ practica, onVerDetalle, etiquetaAccion = 'Ver detalle' }) {
  const badge = BADGE[practica.estado] || { label: practica.estado, variant: 'neutral' };

  return (
    <Card padding="p-4">
      <div className="mb-2 flex items-start justify-between gap-2">
        <div>
          <div className="font-bold text-gray-900">{practica.estudiante?.nombre}</div>
          <div className="text-xs text-gray-500">
            {practica.estudiante?.codigo} — {practica.estudiante?.programa}
          </div>
        </div>
        <Badge variant={badge.variant}>{badge.label}</Badge>
      </div>
      <div className="text-sm text-gray-700">
        <span className="font-semibold">{practica.cargo}</span> · {practica.empresa}
      </div>
      <div className="mt-1 text-xs text-gray-500">Docente: {practica.docente}</div>
      <div className="my-2 h-1.5 overflow-hidden rounded-full bg-gray-200">
        <div
          className="h-full rounded-full bg-primary"
          style={{ width: `${practica.porcentajeAvance || 0}%` }}
        />
      </div>
      <div className="mb-3 text-right text-xs text-gray-500">
        {practica.porcentajeAvance || 0}% completado
      </div>
      <Button size="sm" className="w-full" onClick={() => onVerDetalle(practica.id)}>
        {etiquetaAccion}
      </Button>
    </Card>
  );
}
