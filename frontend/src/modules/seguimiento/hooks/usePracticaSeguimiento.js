// src/modules/seguimiento/hooks/usePracticaSeguimiento.js

/**
 * Hook para obtener el detalle completo de una práctica en el contexto de seguimiento.
 * Incluye timeline de eventos (observaciones, avances, bitácora).
 */
import { useQuery } from '@tanstack/react-query';
import seguimientoService from '../services/seguimientoService';

const MOCK_DETALLE = {
  id: 1,
  estudiante: { nombre: 'Ana García', codigo: '2021001', programa: 'Ing. Sistemas' },
  empresa: 'Tech Solutions SAS',
  cargo: 'Desarrollador Frontend',
  docente: 'Prof. Martínez',
  tutor: 'Ing. Ramírez',
  estado: 'AL_DIA',
  fechaInicio: '2024-02-01',
  fechaFin: '2024-07-31',
  porcentajeAvance: 75,
  timeline: [
    { id: 1, tipo: 'OBSERVACION', autor: 'Prof. Martínez', fecha: '2024-03-10', contenido: 'El estudiante muestra buen desempeño en las tareas asignadas.' },
    { id: 2, tipo: 'AVANCE_TUTOR', autor: 'Ing. Ramírez', fecha: '2024-03-08', contenido: 'Avance del 75%. Completó módulo de autenticación.', porcentaje: 75 },
    { id: 3, tipo: 'BITACORA', autor: 'Ana García', fecha: '2024-03-07', contenido: 'Actividades: Implementación de componentes React. Aprendizajes: Manejo de estado con hooks.' },
    { id: 4, tipo: 'AVANCE_TUTOR', autor: 'Ing. Ramírez', fecha: '2024-02-28', contenido: 'Avance del 50%. Integración con API completada.', porcentaje: 50 },
    { id: 5, tipo: 'OBSERVACION', autor: 'Prof. Martínez', fecha: '2024-02-20', contenido: 'Primera visita de seguimiento realizada satisfactoriamente.' },
  ],
};

export function usePracticaSeguimiento(practicaId) {
  const { data, isLoading, isError } = useQuery({
    queryKey: ['practicaSeguimiento', practicaId],
    queryFn: async () => {
      const resp = await seguimientoService.tablero({ practicaId });
      return resp.data?.data || resp.data || {};
    },
    enabled: !!practicaId,
    placeholderData: MOCK_DETALLE,
  });

  // El timeline ordenado del más reciente al más antiguo
  const timeline = [...(data?.timeline || [])].sort(
    (a, b) => new Date(b.fecha) - new Date(a.fecha)
  );

  return { practica: data, timeline, isLoading, isError };
}
