import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { FiltrosActivos } from '@/shared/components/filtros';
import empresaService from '../../empresa/services/empresaService';
import http from '../../../shared/services/http';

const ESTADOS = [
  { value: 'PENDIENTE_APROBACION', label: 'Pendiente aprobación' },
  { value: 'ACTIVA', label: 'Activa' },
  { value: 'PAUSADA', label: 'Pausada' },
  { value: 'CUPOS_COMPLETOS', label: 'Cupos completos' },
  { value: 'CERRADA', label: 'Cerrada' },
  { value: 'RECHAZADA', label: 'Rechazada' },
];

const MODALIDADES = [
  { value: 'PRESENCIAL', label: 'Presencial' },
  { value: 'REMOTO', label: 'Remoto' },
  { value: 'HÍBRIDO', label: 'Híbrido' },
];

export default function VacantesFiltros({ filtros, setFiltros }) {
  const { data: empresas = [] } = useQuery({
    queryKey: ['empresas-filtro-vacantes'],
    queryFn: () =>
      empresaService.listar({ page: 0, size: 500, activo: true }).then((r) => r.data?.content ?? []),
    staleTime: 60_000,
  });

  const { data: programas = [] } = useQuery({
    queryKey: ['programas-filtro-vacantes'],
    queryFn: () => http.get('/programas').then((r) => r.data ?? []),
    staleTime: 60_000,
  });

  const campos = useMemo(
    () => [
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
      {
        key: 'modalidad',
        label: 'Modalidad',
        type: 'select',
        opciones: MODALIDADES,
      },
      {
        key: 'area',
        label: 'Área',
        type: 'text',
        placeholder: 'Ej: Tecnología',
      },
    ],
    [empresas, programas]
  );

  return (
    <FiltrosActivos
      campos={campos}
      filtros={filtros}
      onChange={setFiltros}
    />
  );
}
