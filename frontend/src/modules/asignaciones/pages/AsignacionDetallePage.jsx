// src/modules/asignaciones/pages/AsignacionDetallePage.jsx

import { useParams, useNavigate } from 'react-router-dom';
import { Building2, ClipboardList, User } from 'lucide-react';
import { useAsignacionDetalle } from '../hooks/useAsignacionDetalle';
import BadgeAsignacion from '../components/BadgeAsignacion';
import HistorialEstados from '../components/HistorialEstados';
import { Button, Card, PageHeader } from '@/shared/components/ui';

function Seccion({ titulo, icono: Icono, children }) {
  return (
    <Card padding="p-0" className="mb-4 overflow-hidden">
      <h2 className="flex items-center gap-2 border-b border-gray-200 bg-slate-50 px-4 py-3 text-sm font-bold text-gray-700">
        {Icono && <Icono className="h-4 w-4" aria-hidden="true" />}
        {titulo}
      </h2>
      <div className="divide-y divide-gray-100 px-4 py-2">{children}</div>
    </Card>
  );
}

function Fila({ label, valor }) {
  return (
    <div className="flex gap-3 py-2 text-sm">
      <span className="min-w-[100px] font-medium text-gray-500">{label}</span>
      <span className="text-gray-900">{valor || '—'}</span>
    </div>
  );
}

export default function AsignacionDetallePage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { asignacion, isLoading, isError } = useAsignacionDetalle(id);

  if (isLoading) {
    return <p className="py-16 text-center text-sm text-gray-500">Cargando detalle de la asignación...</p>;
  }

  if (isError || !asignacion) {
    return (
      <div className="mx-auto max-w-lg py-16 text-center">
        <div className="rounded-lg border border-red-200 bg-red-50 p-6 text-red-800">
          <p className="mb-4">No se pudo cargar la asignación. Verifica el ID o intenta más tarde.</p>
          <Button variant="ghost" size="sm" onClick={() => navigate('/asignaciones')}>
            ← Volver al listado
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-2xl">
      <Button variant="ghost" size="sm" className="mb-4" onClick={() => navigate('/asignaciones')}>
        ← Volver
      </Button>

      <PageHeader
        titulo="Detalle de asignación"
        acciones={<BadgeAsignacion estado={asignacion.estado} />}
      />

      <Seccion titulo="Estudiante" icono={User}>
        <Fila label="Nombre" valor={asignacion.estudiante?.nombre} />
        <Fila label="Código" valor={asignacion.estudiante?.codigo} />
        <Fila label="Programa" valor={asignacion.estudiante?.programa} />
      </Seccion>

      <Seccion titulo="Vacante y empresa" icono={Building2}>
        <Fila label="Cargo" valor={asignacion.vacante?.cargo} />
        <Fila label="Empresa" valor={asignacion.vacante?.empresa} />
        <Fila label="Modalidad" valor={asignacion.vacante?.modalidad} />
      </Seccion>

      <Seccion titulo="Datos de la asignación" icono={ClipboardList}>
        <Fila
          label="Fecha"
          valor={
            asignacion.fechaAsignacion
              ? new Date(asignacion.fechaAsignacion).toLocaleDateString('es-CO', {
                  day: '2-digit',
                  month: 'long',
                  year: 'numeric',
                })
              : '—'
          }
        />
        <Fila label="Estado" valor={<BadgeAsignacion estado={asignacion.estado} />} />
      </Seccion>

      <Seccion titulo="Historial de estados" icono={ClipboardList}>
        <HistorialEstados historial={asignacion.historial || []} />
      </Seccion>
    </div>
  );
}
