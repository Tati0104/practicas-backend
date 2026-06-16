import { useEffect, useRef, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import useAuthStore from '@/store/authStore';
import http from '@/shared/services/http';
import seguimientoService from '../services/seguimientoService';
import { MOCK_PRACTICAS } from '@/shared/mocks/datos';
import {
  ejecutarConsulta,
  paginarEnCliente,
  placeholderDesdeMock,
  usarMocks,
} from '@/shared/config/dataSource';

const ROLES_CON_PROGRAMA_GLOBAL = ['ADMIN', 'COORD_PRACTICA', 'SECRETARIA', 'DIRECCION'];

export function useSeguimiento() {
  const programaIdSesion = useAuthStore((state) => state.programaId);
  const rol = useAuthStore((state) => state.rol);
  const token = useAuthStore((state) => state.token);

  const [filtros, setFiltros] = useState({
    page: 0,
    size: 10,
    programaId: programaIdSesion || (usarMocks() ? '1' : ''),
    docenteId: '',
    estado: '',
    busqueda: '',
  });

  const { data: programas = [] } = useQuery({
    queryKey: ['programas-seguimiento'],
    queryFn: async () => {
      const resp = await http.get('/programas');
      const lista = resp.data?.data ?? resp.data ?? [];
      return Array.isArray(lista) ? lista : [];
    },
    enabled: Boolean(token) && !usarMocks(),
    staleTime: 60_000,
  });

  const autoSeleccionInicial = useRef(false);

  useEffect(() => {
    if (usarMocks() || filtros.programaId || autoSeleccionInicial.current) return;

    if (programaIdSesion) {
      autoSeleccionInicial.current = true;
      setFiltros((prev) => ({ ...prev, programaId: String(programaIdSesion) }));
      return;
    }

    if (ROLES_CON_PROGRAMA_GLOBAL.includes(rol) && programas.length > 0) {
      autoSeleccionInicial.current = true;
      setFiltros((prev) => ({ ...prev, programaId: String(programas[0].id) }));
    }
  }, [filtros.programaId, programaIdSesion, programas, rol]);

  const { data, isLoading, isError, isFetching } = useQuery({
    queryKey: ['seguimiento', filtros, usarMocks()],
    queryFn: () =>
      ejecutarConsulta({
        mock: () => paginarEnCliente(MOCK_PRACTICAS, filtros),
        api: async () => {
          const resp = await seguimientoService.tablero(filtros);
          const lista = resp.data?.data ?? resp.data ?? [];
          const items = Array.isArray(lista) ? lista : (lista.content ?? []);
          return paginarEnCliente(items, filtros);
        },
      }),
    placeholderData: placeholderDesdeMock(MOCK_PRACTICAS),
    enabled: usarMocks() || (Boolean(token) && Boolean(filtros.programaId)),
  });

  const actualizarFiltros = (cambios) =>
    setFiltros((prev) => ({ ...prev, ...cambios, page: 0 }));

  return {
    practicas: data?.content ?? [],
    totalElementos: data?.totalElements ?? 0,
    totalPaginas: data?.totalPages ?? 1,
    isLoading,
    isFetching,
    isError,
    filtros,
    setFiltros,
    actualizarFiltros,
    irAPagina: (pagina) => setFiltros((f) => ({ ...f, page: pagina })),
    requierePrograma: !usarMocks() && !filtros.programaId,
    programasDisponibles: programas.length,
  };
}
