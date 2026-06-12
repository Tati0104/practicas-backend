// src/modules/seguimiento/hooks/useSeguimiento.js

/**
 * Hook para el tablero de seguimiento con filtros y paginación.
 * Usa TanStack Query v5 para cachear la respuesta.
 * Incluye datos mock para mostrar la interfaz sin backend.
 */
import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import seguimientoService from '../services/seguimientoService';

const MOCK_PRACTICAS = [
  {
    id: 1,
    estudiante: { nombre: 'Ana García', codigo: '2021001', programa: 'Ing. Sistemas' },
    empresa: 'Tech Solutions SAS',
    cargo: 'Desarrollador Frontend',
    docente: 'Prof. Martínez',
    estado: 'AL_DIA',
    fechaInicio: '2024-02-01',
    porcentajeAvance: 75,
    ultimaActividad: '2024-03-10',
  },
  {
    id: 2,
    estudiante: { nombre: 'Carlos Ruiz', codigo: '2021002', programa: 'Ing. Industrial' },
    empresa: 'Constructora ABC',
    cargo: 'Analista de Procesos',
    docente: 'Prof. González',
    estado: 'PENDIENTE',
    fechaInicio: '2024-02-15',
    porcentajeAvance: 30,
    ultimaActividad: '2024-02-20',
  },
  {
    id: 3,
    estudiante: { nombre: 'María López', codigo: '2020003', programa: 'Administración' },
    empresa: 'Logística Express',
    cargo: 'Asistente Administrativo',
    docente: 'Prof. Rodríguez',
    estado: 'EN_ALERTA',
    fechaInicio: '2024-01-20',
    porcentajeAvance: 10,
    ultimaActividad: '2024-01-25',
  },
  {
    id: 4,
    estudiante: { nombre: 'Juan Pérez', codigo: '2021004', programa: 'Ing. Sistemas' },
    empresa: 'Software Corp',
    cargo: 'Desarrollador Backend',
    docente: 'Prof. Martínez',
    estado: 'AL_DIA',
    fechaInicio: '2024-02-10',
    porcentajeAvance: 60,
    ultimaActividad: '2024-03-08',
  },
];

export function useSeguimiento() {
  const [filtros, setFiltros] = useState({
    page: 0,
    size: 10,
    programaId: '',
    docenteId: '',
    estado: '',
    busqueda: '',
  });

  const { data, isLoading, isError, isFetching } = useQuery({
    queryKey: ['seguimiento', filtros],
    queryFn: async () => {
      const resp = await seguimientoService.tablero(filtros);
      return resp.data?.data || resp.data || {};
    },
    placeholderData: { content: MOCK_PRACTICAS, totalElements: 4, totalPages: 1 },
    keepPreviousData: true,
  });

  return {
    practicas: data?.content || [],
    totalElementos: data?.totalElements || 0,
    totalPaginas: data?.totalPages || 1,
    isLoading,
    isFetching,
    isError,
    filtros,
    setFiltros,
    irAPagina: (pagina) => setFiltros((f) => ({ ...f, page: pagina })),
  };
}
