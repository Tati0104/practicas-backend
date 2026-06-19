// src/modules/vacantes/components/VacantesTabla.jsx

import TablaBase from '../../../shared/components/TablaBase';
import BadgeVacanteEstado from './BadgeVacanteEstado';
import { Button } from '@/shared/components/ui';

export default function VacantesTabla({ vacantes, acciones }) {
  const { aprobar, rechazar, pausar, cerrar, canApprove, canReject, canPause, canClose } = acciones;

  const columnas = [
    {
      key: 'empresa',
      titulo: 'Empresa',
      render: (v) => v.empresaNombre ?? (v.empresaId ? `Empresa #${v.empresaId}` : '—'),
    },
    { key: 'cargo', titulo: 'Cargo' },
    {
      key: 'nivelPractica',
      titulo: 'Nivel',
      render: (v) => v.catalogoPracticaNombre ?? (v.numeroPractica ? `PrÃ¡ctica ${v.numeroPractica}` : 'â€”'),
    },
    { key: 'modalidad', titulo: 'Modalidad' },
    {
      key: 'cupos',
      titulo: 'Cupos',
      render: (v) => `${v.cuposDisponibles ?? 0} / ${v.cuposTotales ?? '—'}`,
    },
    {
      key: 'estado',
      titulo: 'Estado',
      render: (v) => <BadgeVacanteEstado estado={v.estado} />,
    },
    {
      key: 'acciones',
      titulo: 'Acciones',
      render: (v) => (
        <div className="flex flex-wrap gap-1.5">
          {canApprove && v.estado === 'PENDIENTE_APROBACION' && (
            <Button variant="success" size="sm" onClick={() => aprobar.mutate(v.id)}>
              Aprobar
            </Button>
          )}
          {canReject && v.estado === 'PENDIENTE_APROBACION' && (
            <Button
              variant="danger"
              size="sm"
              onClick={() => rechazar.mutate({ id: v.id, motivo: 'Rechazado por admin' })}
            >
              Rechazar
            </Button>
          )}
          {canPause && v.estado === 'ACTIVA' && (
            <Button variant="warning" size="sm" onClick={() => pausar.mutate(v.id)}>
              Pausar
            </Button>
          )}
          {canClose && v.estado !== 'CERRADA' && (
            <Button variant="ghost" size="sm" onClick={() => cerrar.mutate(v.id)}>
              Cerrar
            </Button>
          )}
        </div>
      ),
    },
  ];

  return <TablaBase columnas={columnas} datos={vacantes} />;
}
