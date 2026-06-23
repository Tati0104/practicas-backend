import { useMemo } from 'react';
import { FiltrosActivos } from '@/shared/components/filtros';

const ESTADOS = [
  { value: 'AL_DIA', label: 'Al día' },
  { value: 'PENDIENTE', label: 'Pendiente' },
  { value: 'EN_ALERTA', label: 'En alerta' },
];

export default function SeguimientoFiltros({ filtros, setFiltros }) {
  const campos = useMemo(
    () => [
      {
        key: 'busqueda',
        label: 'Búsqueda',
        type: 'text',
        placeholder: 'Buscar estudiante, empresa, cargo…',
      },
      {
        key: 'programaId',
        label: 'Programa',
        type: 'text',
        placeholder: 'ID del programa',
      },
      {
        key: 'estado',
        label: 'Estado de seguimiento',
        type: 'select',
        placeholder: 'Filtrar por estado…',
        opciones: ESTADOS,
      },
    ],
    []
  );

  return <FiltrosActivos campos={campos} filtros={filtros} onChange={setFiltros} />;
}
