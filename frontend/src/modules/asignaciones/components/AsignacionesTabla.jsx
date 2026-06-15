// src/modules/asignaciones/components/AsignacionesTabla.jsx

import { useNavigate } from 'react-router-dom';
import TablaBase from '../../../shared/components/TablaBase';
import BadgeAsignacion from './BadgeAsignacion';
import { Button } from '@/shared/components/ui';

export default function AsignacionesTabla({ asignaciones, isLoading, onCancelar, canCancelar }) {
  const navigate = useNavigate();

  const columnas = [
    {
      key: 'estudiante',
      titulo: 'Estudiante',
      render: (fila) => (
        <div>
          <div className="text-sm font-semibold text-gray-900">{fila.estudiante?.nombre || '—'}</div>
          <div className="text-xs text-gray-500">
            {fila.estudiante?.codigo} · {fila.estudiante?.programa}
          </div>
        </div>
      ),
    },
    {
      key: 'vacante',
      titulo: 'Cargo / Empresa',
      render: (fila) => (
        <div>
          <div className="text-sm font-semibold text-gray-900">{fila.vacante?.cargo || '—'}</div>
          <div className="text-xs text-gray-500">{fila.vacante?.empresa}</div>
        </div>
      ),
    },
    {
      key: 'fechaAsignacion',
      titulo: 'Fecha',
      render: (fila) =>
        fila.fechaAsignacion
          ? new Date(fila.fechaAsignacion).toLocaleDateString('es-CO')
          : '—',
    },
    {
      key: 'estado',
      titulo: 'Estado',
      render: (fila) => <BadgeAsignacion estado={fila.estado} />,
    },
    {
      key: 'acciones',
      titulo: 'Acciones',
      render: (fila) => (
        <div className="flex flex-wrap gap-1.5">
          <Button
            size="sm"
            className="bg-blue-600 hover:bg-blue-700"
            onClick={() => navigate(`/asignaciones/${fila.id}`)}
          >
            Ver
          </Button>
          {canCancelar && fila.estado !== 'CANCELADA' && fila.estado !== 'VINCULADA' && (
            <Button variant="danger" size="sm" onClick={() => onCancelar(fila)}>
              Cancelar
            </Button>
          )}
        </div>
      ),
    },
  ];

  return (
    <TablaBase
      columnas={columnas}
      datos={asignaciones}
      cargando={isLoading}
      sinDatos="No hay asignaciones que coincidan con los filtros."
    />
  );
}
