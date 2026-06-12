// src/modules/asignaciones/hooks/useAsignaciones.js

/**
 * Hook para listar asignaciones con paginación y filtros.
 *
 * Maneja el estado local de filtros (programaId, estado, busqueda, página).
 * Usa TanStack Query v5 para cachear la respuesta.
 * keepPreviousData: true evita que la tabla "parpadee" al cambiar de página.
 */
import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import asignacionService from '../services/asignacionService';

// Datos mock para mostrar algo mientras la API responde (evita pantalla vacía)
const MOCK_ASIGNACIONES = [
  {
    id: 1,
    estudiante: { nombre: 'Ana García', codigo: '2021001', programa: 'Ing. Sistemas' },
    vacante: { cargo: 'Desarrollador Frontend', empresa: 'Tech Solutions SAS' },
    fechaAsignacion: '2024-02-10',
    estado: 'ASIGNADA',
  },
  {
    id: 2,
    estudiante: { nombre: 'Carlos Ruiz', codigo: '2021002', programa: 'Ing. Industrial' },
    vacante: { cargo: 'Analista de Procesos', empresa: 'Constructora ABC' },
    fechaAsignacion: '2024-02-15',
    estado: 'EN_VINCULACION',
  },
  {
    id: 3,
    estudiante: { nombre: 'María López', codigo: '2020003', programa: 'Administración' },
    vacante: { cargo: 'Asistente Administrativo', empresa: 'Logística Express' },
    fechaAsignacion: '2024-01-20',
    estado: 'CANCELADA',
  },
];

export function useAsignaciones() {
  // Estado local de filtros; cada cambio dispara una nueva query
  const [filtros, setFiltros] = useState({
    page: 0,          // página actual (base 0)
    size: 10,         // registros por página
    programaId: '',   // ID del programa académico
    estado: '',       // Estado: ASIGNADA, EN_VINCULACION, VINCULADA, CANCELADA
    busqueda: '',     // texto libre (nombre estudiante, empresa, cargo)
  });

  const { data, isLoading, isError, isFetching } = useQuery({
    // La queryKey incluye filtros para que cada combinación tenga su caché propio
    queryKey: ['asignaciones', filtros],
    queryFn: async () => {
      const resp = await asignacionService.listar(filtros);
      // El backend devuelve Page<Asignacion>: { content, totalElements, totalPages }
      return resp.data?.data || resp.data || {};
    },
    // Mientras la API responde, mostramos el mock
    placeholderData: { content: MOCK_ASIGNACIONES, totalElements: 3, totalPages: 1 },
    // Mantiene datos anteriores mientras carga la nueva página (no parpadea)
    keepPreviousData: true,
  });

  return {
    // Lista de asignaciones de la página actual
    asignaciones: data?.content || [],
    totalElementos: data?.totalElements || 0,
    totalPaginas: data?.totalPages || 1,
    isLoading,
    isFetching,
    isError,
    filtros,
    setFiltros,
    // Cambia de página actualizando el filtro `page`
    irAPagina: (pagina) => setFiltros((f) => ({ ...f, page: pagina })),
  };
}
