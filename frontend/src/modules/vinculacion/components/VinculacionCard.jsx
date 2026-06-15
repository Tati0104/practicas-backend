// src/modules/vinculacion/components/VinculacionCard.jsx

import { useNavigate } from 'react-router-dom';
import BadgeDocumento from './BadgeDocumento';
import { Button, Card } from '@/shared/components/ui';

export default function VinculacionCard({ vinculacion, onGestionar }) {
  const navigate = useNavigate();

  const carta = vinculacion.documentos?.find((d) => d.tipo === 'CARTA');
  const convenio = vinculacion.documentos?.find((d) => d.tipo === 'CONVENIO');
  const firmados = vinculacion.documentos?.filter((d) => d.estado === 'FIRMADO').length || 0;
  const total = vinculacion.documentos?.length || 2;

  const irADetalle = () =>
    onGestionar
      ? onGestionar(vinculacion)
      : navigate(`/vinculacion/${vinculacion.practicaId}`);

  return (
    <Card padding="p-4">
      <div className="mb-2 flex items-start justify-between gap-2">
        <div>
          <div className="font-bold text-gray-900">{vinculacion.estudiante?.nombre || '—'}</div>
          <div className="text-xs text-gray-500">
            {vinculacion.estudiante?.codigo} · {vinculacion.estudiante?.programa}
          </div>
        </div>
        <span
          className={`text-sm font-bold ${firmados === total ? 'text-emerald-600' : 'text-gray-700'}`}
        >
          {firmados}/{total}
        </span>
      </div>

      <dl className="space-y-1 text-sm">
        <div className="flex gap-2">
          <dt className="min-w-[70px] text-gray-500">Cargo:</dt>
          <dd className="font-medium text-gray-900">{vinculacion.vacante?.cargo || '—'}</dd>
        </div>
        <div className="flex gap-2">
          <dt className="min-w-[70px] text-gray-500">Empresa:</dt>
          <dd className="font-medium text-gray-900">{vinculacion.vacante?.empresa || '—'}</dd>
        </div>
      </dl>

      <div className="mt-3 flex flex-wrap gap-3">
        <div className="flex items-center gap-2">
          <span className="text-xs text-gray-500">Carta:</span>
          <BadgeDocumento estado={carta?.estado || 'PENDIENTE'} />
        </div>
        <div className="flex items-center gap-2">
          <span className="text-xs text-gray-500">Convenio:</span>
          <BadgeDocumento estado={convenio?.estado || 'PENDIENTE'} />
        </div>
      </div>

      <Button
        size="sm"
        className="mt-3 w-full bg-blue-600 hover:bg-blue-700"
        onClick={irADetalle}
        aria-label={`Gestionar vinculación de ${vinculacion.estudiante?.nombre}`}
      >
        Gestionar
      </Button>
    </Card>
  );
}
