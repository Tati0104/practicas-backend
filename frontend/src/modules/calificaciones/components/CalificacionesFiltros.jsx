import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { FiltrosActivos } from '@/shared/components/filtros';
import http from '@/shared/services/http';

const ESTADOS_SEGUIMIENTO = [
  { value: 'AL_DIA', label: 'Al día' },
  { value: 'PENDIENTE', label: 'Pendiente' },
  { value: 'EN_ALERTA', label: 'En alerta' },
];

export default function CalificacionesFiltros({ filtros, setFiltros }) {
  const { data: programas = [] } = useQuery({
    queryKey: ['programas-filtro-calificaciones'],
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
        label: 'Estado seguimiento',
        type: 'select',
        opciones: ESTADOS_SEGUIMIENTO,
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

  return <FiltrosActivos campos={campos} filtros={filtros} onChange={setFiltros} />;
}
