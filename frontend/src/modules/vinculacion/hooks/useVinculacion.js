// src/modules/vinculacion/hooks/useVinculacion.js

/**
 * Hook: useVinculacion
 * ─────────────────────
 * Obtiene la lista paginada de procesos de vinculación activos.
 *
 * ¿Por qué este hook?
 *   La página principal de Vinculación necesita listar TODAS las asignaciones que
 *   están en proceso de vinculación (con carta y/o convenio pendiente). Este hook
 *   encapsula la llamada a la API, los filtros y la paginación.
 *
 * Endpoint usado:
 *   GET /api/practicas/documentos?page=&size=&programaId=&estado=&busqueda=
 *   (Nota: si el backend no expone este endpoint, se puede adaptar a
 *    GET /api/asignaciones?estado=EN_VINCULACION o similar)
 *
 * Estructura de respuesta esperada:
 *   {
 *     content: [
 *       {
 *         practicaId:   number,
 *         asignacionId: number,
 *         estudiante:   { nombre, codigo, programa },
 *         vacante:      { cargo, empresa },
 *         documentos:   [ { id, tipo, nombre, estado, firmas[] } ]
 *       }
 *     ],
 *     totalElements: number,
 *     totalPages:    number,
 *     number:        number   ← página actual (base 0)
 *   }
 *
 * Exporta:
 *   vinculaciones  → array de la página actual
 *   totalPaginas   → para la paginación
 *   totalElementos → para mostrar "X registros"
 *   isLoading      → spinner
 *   isFetching     → indicador de recarga en segundo plano
 *   isError        → para mostrar estado de error
 *   filtros        → objeto de filtros actuales
 *   setFiltros     → actualizador de filtros
 *   irAPagina      → función helper para cambiar de página
 */

import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import http from '../../../shared/services/http.js';

// ── Datos mock para mostrar mientras llega la API ──────────────────────────
const MOCK_VINCULACIONES = [
  {
    practicaId:   101,
    asignacionId: 1,
    estudiante:   { nombre: 'Ana García', codigo: '2021001', programa: 'Ing. Sistemas' },
    vacante:      { cargo: 'Desarrollador Frontend', empresa: 'Tech Solutions SAS' },
    documentos: [
      {
        id: 1, tipo: 'CARTA', nombre: 'Carta_AnaGarcia.pdf', estado: 'SUBIDO',
        firmas: [
          { tipoFirmante: 'COORDINADOR', firmado: true,  fechaFirma: '2024-03-01' },
          { tipoFirmante: 'TUTOR',       firmado: false, fechaFirma: null },
          { tipoFirmante: 'ESTUDIANTE',  firmado: false, fechaFirma: null },
        ],
      },
      {
        id: 2, tipo: 'CONVENIO', nombre: null, estado: 'PENDIENTE',
        firmas: [
          { tipoFirmante: 'COORDINADOR', firmado: false, fechaFirma: null },
          { tipoFirmante: 'TUTOR',       firmado: false, fechaFirma: null },
          { tipoFirmante: 'ESTUDIANTE',  firmado: false, fechaFirma: null },
        ],
      },
    ],
  },
  {
    practicaId:   102,
    asignacionId: 2,
    estudiante:   { nombre: 'Carlos Ruiz', codigo: '2021002', programa: 'Ing. Industrial' },
    vacante:      { cargo: 'Analista de Procesos', empresa: 'Constructora ABC' },
    documentos: [
      {
        id: 3, tipo: 'CARTA', nombre: 'Carta_CarlosRuiz.pdf', estado: 'FIRMADO',
        firmas: [
          { tipoFirmante: 'COORDINADOR', firmado: true, fechaFirma: '2024-03-05' },
          { tipoFirmante: 'TUTOR',       firmado: true, fechaFirma: '2024-03-06' },
          { tipoFirmante: 'ESTUDIANTE',  firmado: true, fechaFirma: '2024-03-07' },
        ],
      },
      {
        id: 4, tipo: 'CONVENIO', nombre: 'Convenio_CarlosRuiz.pdf', estado: 'SUBIDO',
        firmas: [
          { tipoFirmante: 'COORDINADOR', firmado: true,  fechaFirma: '2024-03-08' },
          { tipoFirmante: 'TUTOR',       firmado: false, fechaFirma: null },
          { tipoFirmante: 'ESTUDIANTE',  firmado: false, fechaFirma: null },
        ],
      },
    ],
  },
];

export function useVinculacion() {
  // Estado de filtros: se inicializa con valores vacíos
  const [filtros, setFiltros] = useState({
    page:       0,
    size:       10,
    programaId: '',
    estado:     '',
    busqueda:   '',
  });

  const { data, isLoading, isFetching, isError } = useQuery({
    // queryKey incluye filtros → React Query crea una entrada de caché por combinación
    queryKey: ['vinculacion', filtros],
    queryFn: async () => {
      const resp = await http.get('/api/practicas/documentos', { params: filtros });
      // Intentamos el formato estándar del backend; si no viene envuelto, usamos directo
      return resp.data?.data || resp.data || {};
    },
    // Datos de relleno mientras carga la primera vez
    placeholderData: {
      content:       MOCK_VINCULACIONES,
      totalElements: MOCK_VINCULACIONES.length,
      totalPages:    1,
      number:        0,
    },
    // Mantiene los datos anteriores visible mientras carga la nueva página
    keepPreviousData: true,
  });

  return {
    vinculaciones:   data?.content       || [],
    totalElementos:  data?.totalElements || 0,
    totalPaginas:    data?.totalPages    || 1,
    isLoading,
    isFetching,
    isError,
    filtros,
    setFiltros,
    // Helper para cambiar de página sin repetir la lógica en el componente
    irAPagina: (pagina) => setFiltros((f) => ({ ...f, page: pagina })),
  };
}
