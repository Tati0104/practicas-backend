// src/modules/vacantes/components/VacanteCard.jsx

import BadgeVacanteEstado from './BadgeVacanteEstado';
import { Button, Card } from '@/shared/components/ui';

export default function VacanteCard({ vacante, acciones }) {
  const { aprobar, rechazar, pausar, reanudar, cerrar, canApprove, canReject, canPause, canClose } = acciones;

  return (
    <Card padding="p-4">
      <div className="mb-2 flex items-start justify-between gap-2">
        <div>
          <h3 className="font-semibold text-gray-900">{vacante.cargo}</h3>
          <p className="text-sm text-gray-600">
            {vacante.empresaNombre ?? (vacante.empresaId ? `Empresa #${vacante.empresaId}` : 'Sin empresa')}
          </p>
        </div>
        <BadgeVacanteEstado estado={vacante.estado} />
      </div>
      <p className="text-sm text-gray-600">{vacante.modalidad}</p>
      <p className="text-sm text-gray-600">
        Cupos: {vacante.cuposDisponibles ?? 0} / {vacante.cuposTotales ?? '—'}
      </p>
      <div className="mt-3 flex flex-wrap gap-1.5">
        {canApprove && vacante.estado === 'PENDIENTE_APROBACION' && (
          <Button variant="success" size="sm" onClick={() => aprobar.mutate(vacante.id)}>
            Aprobar
          </Button>
        )}
        {canReject && vacante.estado === 'PENDIENTE_APROBACION' && (
          <Button
            variant="danger"
            size="sm"
            onClick={() => rechazar.mutate({ id: vacante.id, motivo: 'Rechazado por UI' })}
          >
            Rechazar
          </Button>
        )}
        {canPause && vacante.estado === 'ACTIVA' && (
          <Button variant="warning" size="sm" onClick={() => pausar.mutate(vacante.id)}>
            Pausar
          </Button>
        )}
        {canPause && vacante.estado === 'PAUSADA' && (
          <Button variant="success" size="sm" onClick={() => reanudar.mutate(vacante.id)}>
            Reanudar
          </Button>
        )}
        {canClose && vacante.estado !== 'CERRADA' && (
          <Button variant="ghost" size="sm" onClick={() => cerrar.mutate(vacante.id)}>
            Cerrar
          </Button>
        )}
      </div>
    </Card>
  );
}
