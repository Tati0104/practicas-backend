// src/modules/vacantes/pages/VacanteDetallePage.jsx

import { useParams } from 'react-router-dom';
import { useVacanteDetalle } from '../hooks/useVacanteDetalle';
import HistorialEstados from '../components/HistorialEstados';
import { Card, PageHeader } from '@/shared/components/ui';

export default function VacanteDetallePage() {
  const { id } = useParams();
  const { vacante, isLoading, isError } = useVacanteDetalle(id);

  if (isLoading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <span className="text-sm text-gray-600">Cargando detalle de la vacante...</span>
      </div>
    );
  }

  if (isError) {
    return (
      <div className="rounded-lg border border-red-200 bg-red-50 p-4 text-sm text-red-800">
        Hubo un error al cargar la vacante.
      </div>
    );
  }

  if (!vacante) {
    return (
      <div className="p-4 text-sm text-gray-600">
        <p>No se encontró la vacante.</p>
      </div>
    );
  }

  const { empresa, cargo, modalidad, cuposTotal, cuposDisponibles, estado } = vacante;

  const campos = [
    { label: 'Empresa', valor: empresa },
    { label: 'Cargo', valor: cargo },
    { label: 'Modalidad', valor: modalidad },
    { label: 'Estado', valor: estado },
    { label: 'Cupos totales', valor: cuposTotal },
    { label: 'Cupos disponibles', valor: cuposDisponibles },
  ];

  return (
    <div className="mx-auto max-w-4xl">
      <PageHeader titulo="Detalle de vacante" descripcion={`Vacante #${id}`} />

      <Card className="mb-6">
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          {campos.map(({ label, valor }) => (
            <div key={label}>
              <p className="text-xs font-semibold uppercase tracking-wide text-gray-400">
                {label}
              </p>
              <p className="text-sm font-medium text-gray-900">{valor ?? '—'}</p>
            </div>
          ))}
        </div>
      </Card>

      <Card>
        <h2 className="mb-4 text-base font-bold text-gray-900">Historial de estados</h2>
        <HistorialEstados vacanteId={id} />
      </Card>
    </div>
  );
}
