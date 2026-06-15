// src/modules/vinculacion/components/VinculacionTabla.jsx

import { useNavigate } from 'react-router-dom';
import TablaBase from '../../../shared/components/TablaBase';
import BadgeDocumento from './BadgeDocumento';
import { Button } from '@/shared/components/ui';

export default function VinculacionTabla({ vinculaciones, isLoading, onGestionar }) {
  const navigate = useNavigate();

  const contarFirmados = (docs = []) => docs.filter((d) => d.estado === 'FIRMADO').length;

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
      key: 'carta',
      titulo: 'Carta',
      render: (fila) => {
        const carta = fila.documentos?.find((d) => d.tipo === 'CARTA');
        return <BadgeDocumento estado={carta?.estado || 'PENDIENTE'} />;
      },
    },
    {
      key: 'convenio',
      titulo: 'Convenio',
      render: (fila) => {
        const convenio = fila.documentos?.find((d) => d.tipo === 'CONVENIO');
        return <BadgeDocumento estado={convenio?.estado || 'PENDIENTE'} />;
      },
    },
    {
      key: 'progreso',
      titulo: 'Progreso',
      render: (fila) => {
        const firmados = contarFirmados(fila.documentos);
        const total = fila.documentos?.length || 2;
        return (
          <span
            className={`text-sm font-semibold ${firmados === total ? 'text-emerald-600' : 'text-gray-700'}`}
          >
            {firmados}/{total} firmados
          </span>
        );
      },
    },
    {
      key: 'acciones',
      titulo: 'Acciones',
      render: (fila) => (
        <Button
          size="sm"
          className="bg-blue-600 hover:bg-blue-700"
          onClick={() =>
            onGestionar
              ? onGestionar(fila)
              : navigate(`/vinculacion/${fila.asignacionId ?? fila.practicaId}`)
          }
          aria-label={`Gestionar vinculación de ${fila.estudiante?.nombre}`}
        >
          Gestionar
        </Button>
      ),
    },
  ];

  return (
    <TablaBase
      columnas={columnas}
      datos={vinculaciones}
      cargando={isLoading}
      sinDatos="No hay procesos de vinculación que coincidan con los filtros."
    />
  );
}
