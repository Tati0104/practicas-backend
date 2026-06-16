// src/modules/asignaciones/components/AsignacionCard.jsx

import { useNavigate } from 'react-router-dom';
import BadgeAsignacion from './BadgeAsignacion';
import { Button, Card } from '@/shared/components/ui';

export default function AsignacionCard({
  asignacion,
  onCancelar,
  canCancelar,
  onAsignarDocente,
  canAsignarDocente,
}) {
  const navigate = useNavigate();

  return (
    <Card padding="p-4">
      <div className="mb-2 flex items-start justify-between gap-2">
        <div>
          <div className="font-bold text-gray-900">{asignacion.estudiante?.nombre || '—'}</div>
          <div className="text-xs text-gray-500">
            {asignacion.estudiante?.codigo} · {asignacion.estudiante?.programa}
          </div>
        </div>
        <BadgeAsignacion estado={asignacion.estado} />
      </div>

      <dl className="space-y-1 text-sm">
        <div className="flex gap-2">
          <dt className="min-w-[70px] text-gray-500">Cargo:</dt>
          <dd className="font-medium text-gray-900">{asignacion.vacante?.cargo || '—'}</dd>
        </div>
        <div className="flex gap-2">
          <dt className="min-w-[70px] text-gray-500">Empresa:</dt>
          <dd className="font-medium text-gray-900">{asignacion.vacante?.empresa || '—'}</dd>
        </div>
        <div className="flex gap-2">
          <dt className="min-w-[70px] text-gray-500">Fecha:</dt>
          <dd className="font-medium text-gray-900">
            {asignacion.fechaAsignacion
              ? new Date(asignacion.fechaAsignacion).toLocaleDateString('es-CO')
              : '—'}
          </dd>
        </div>
      </dl>

      <div className="mt-3 flex gap-2">
        <Button
          size="sm"
          className="flex-1 bg-blue-600 hover:bg-blue-700"
          onClick={() => navigate(`/asignaciones/${asignacion.id}`)}
        >
          Ver detalle
        </Button>
        {canCancelar &&
          asignacion.estado !== 'CANCELADA' &&
          asignacion.estado !== 'VINCULADA' && (
            <Button variant="danger" size="sm" className="flex-1" onClick={() => onCancelar(asignacion)}>
              Cancelar
            </Button>
          )}
        {canAsignarDocente && asignacion.estado !== 'CANCELADA' && (
          <Button variant="info" size="sm" className="flex-1" onClick={() => onAsignarDocente(asignacion)}>
            Asignar Docente
          </Button>
        )}
      </div>
    </Card>
  );
}
