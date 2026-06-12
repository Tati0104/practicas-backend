// src/modules/vacantes/hooks/useVacantes.js
/*
  Hook encargado de obtener la lista paginada de vacantes.
  - Utiliza useQuery de TanStack Query v5.
  - Aplica filtros mediante estado local (empresaId, programaId, estado, búsqueda, página y tamaño).
  - Usa datos mock como initialData para evitar flicker mientras carga la API real.
  - Exporta además los setters de filtros para que los componentes de filtro actualicen la query.
*/
import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import empresaService from '../../services/empresaService'; // Servicio compartido

// Mock de datos de vacantes (se usa mientras la petición real responde)
const MOCK_VACANTES = [
  { id: 1, empresa: 'Tech Solutions SAS', cargo: 'Desarrollador Backend', modalidad: 'PRESENCIAL', cuposTotal: 2, cuposDisponibles: 1, estado: 'ACTIVA' },
  { id: 2, empresa: 'Constructora ABC',   cargo: 'Ing. Residente de Obra', modalidad: 'PRESENCIAL', cuposTotal: 1, cuposDisponibles: 1, estado: 'PENDIENTE_APROBACION' },
  { id: 3, empresa: 'Tech Solutions SAS', cargo: 'Analista de Datos', modalidad: 'REMOTO', cuposTotal: 3, cuposDisponibles: 0, estado: 'CUPOS_COMPLETOS' },
];

/**
 * Hook que devuelve la lista de vacantes y utilidades relacionadas.
 */
export function useVacantes() {
  const queryClient = useQueryClient();
  // filtros: empresaId, programaId, estado, busqueda, página, tamaño
  const [filtros, setFiltros] = useState({
    pagina: 0,
    tamano: 10,
    empresaId: undefined,
    programaId: undefined,
    estado: undefined,
    busqueda: ''
  });

  const { data, isLoading, isError } = useQuery({
    queryKey: ['vacantes', filtros],
    queryFn: async () => {
      // El servicio devuelve un objeto con la lista bajo data.content
      const resp = await empresaService.listarVacantes(filtros);
      return resp.data.data?.content || [];
    },
    // Mientras la llamada real está pendiente, mostramos el mock para una UI más fluida.
    initialData: MOCK_VACANTES,
    // Habilitamos refetch automático cuando cambian los filtros.
    keepPreviousData: true,
  });

  // Exponemos la lista (o arreglo vacío), estado de carga y funciones de filtro.
  return {
    vacantes: data || [],
    isLoading,
    isError,
    filtros,
    setFiltros,
    // Permite refrescar manualmente si algún componente lo necesita.
    refetch: () => queryClient.invalidateQueries(['vacantes'])
  };
}
