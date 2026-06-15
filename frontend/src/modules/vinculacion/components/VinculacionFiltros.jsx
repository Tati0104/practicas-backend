import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { FiltrosActivos } from '@/shared/components/filtros';
import empresaService from '../../empresa/services/empresaService';
import http from '../../../shared/services/http';

const ESTADOS = [
  { value: 'ASIGNADA', label: 'Asignada' },
  { value: 'EN_PROCESO_VINCULACION', label: 'En proceso de vinculación' },
  { value: 'VINCULADA', label: 'Vinculada' },
];

export default function VinculacionFiltros({ filtros, setFiltros }) {
  const { data: empresas = [] } = useQuery({
    queryKey: ['empresas-filtro-vinculacion'],
    queryFn: () =>
      empresaService.listar({ page: 0, size: 500, activo: true }).then((r) => r.data?.content ?? []),
    staleTime: 60_000,
  });

  const { data: programas = [] } = useQuery({
    queryKey: ['programas-filtro-vinculacion'],
    queryFn: () => http.get('/programas').then((r) => r.data ?? []),
    staleTime: 60_000,
  });

  const campos = useMemo(
    () => [
      {
        key: 'busqueda',
        label: 'Búsqueda',
        type: 'text',
        placeholder: 'Estudiante, empresa, cargo…',
      },
      {
        key: 'empresaId',
        label: 'Empresa',
        type: 'select',
        opciones: empresas.map((e) => ({
          value: String(e.id),
          label: e.razonSocial ?? e.nombre ?? `Empresa ${e.id}`,
        })),
      },
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
    ],
    [empresas, programas]
  );

  return <FiltrosActivos campos={campos} filtros={filtros} onChange={setFiltros} />;
}
