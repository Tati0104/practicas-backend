import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { FiltrosActivos } from '@/shared/components/filtros';
import http from '../../../shared/services/http';

const ESTADOS = [
  { value: 'ASIGNADA', label: 'Asignada' },
  { value: 'EN_PROCESO_VINCULACION', label: 'En vinculación' },
  { value: 'VINCULADA', label: 'Vinculada' },
  { value: 'CANCELADA', label: 'Cancelada' },
];

export default function AsignacionesFiltros({ filtros, setFiltros }) {
  const { data: programas = [] } = useQuery({
    queryKey: ['programas-filtro-asignaciones'],
    queryFn: () => http.get('/programas').then((r) => r.data ?? []),
    staleTime: 60_000,
  });

  const campos = useMemo(
    () => [
      {
        key: 'programaId',
        label: 'Programa',
        type: 'select',
        opciones: programas.map((p) => ({
          value: String(p.id),
          label: p.nombre,
        })),
      },
      {
        key: 'estado',
        label: 'Estado',
        type: 'select',
        opciones: ESTADOS,
      },
      {
        key: 'busqueda',
        label: 'Búsqueda',
        type: 'text',
        placeholder: 'Estudiante, empresa, cargo…',
      },
    ],
    [programas]
  );

  return (
    <FiltrosActivos
      campos={campos}
      filtros={filtros}
      onChange={setFiltros}
    />
  );
}
