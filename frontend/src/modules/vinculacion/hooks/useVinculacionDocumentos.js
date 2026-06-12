// src/modules/vinculacion/hooks/useVinculacionDocumentos.js

/**
 * Hook que obtiene la lista de documentos de una práctica.
 * GET /api/practicas/{practicaId}/documentos
 *
 * Estructura de respuesta esperada del backend:
 * [
 *   {
 *     id: number,
 *     tipo: 'CARTA' | 'CONVENIO',
 *     nombre: string,
 *     estado: 'PENDIENTE' | 'SUBIDO' | 'FIRMADO',
 *     firmas: [
 *       { tipoFirmante: 'COORDINADOR' | 'TUTOR' | 'ESTUDIANTE', firmado: boolean, fechaFirma: string }
 *     ]
 *   }
 * ]
 *
 * enabled: solo ejecuta si hay practicaId válido.
 */
import { useQuery } from '@tanstack/react-query';
import vinculacionService from '../services/vinculacionService';

// Datos mock para mostrar algo mientras llega la API
const MOCK_DOCUMENTOS = [
  {
    id: 1,
    tipo: 'CARTA',
    nombre: 'Carta_presentacion_AnaGarcia.pdf',
    estado: 'SUBIDO',
    firmas: [
      { tipoFirmante: 'COORDINADOR', firmado: true,  fechaFirma: '2024-03-01' },
      { tipoFirmante: 'TUTOR',       firmado: false, fechaFirma: null },
      { tipoFirmante: 'ESTUDIANTE',  firmado: false, fechaFirma: null },
    ],
  },
  {
    id: 2,
    tipo: 'CONVENIO',
    nombre: null,
    estado: 'PENDIENTE',
    firmas: [
      { tipoFirmante: 'COORDINADOR', firmado: false, fechaFirma: null },
      { tipoFirmante: 'TUTOR',       firmado: false, fechaFirma: null },
      { tipoFirmante: 'ESTUDIANTE',  firmado: false, fechaFirma: null },
    ],
  },
];

export function useVinculacionDocumentos(practicaId) {
  const { data, isLoading, isError, refetch } = useQuery({
    queryKey: ['vinculacion-documentos', practicaId],
    queryFn: () =>
      vinculacionService
        .obtenerDocumentos(practicaId)
        .then((r) => r.data?.data || r.data || []),
    // Solo activa la query si hay un practicaId real
    enabled: !!practicaId,
    // Mientras carga, mostramos el mock
    placeholderData: MOCK_DOCUMENTOS,
  });

  return {
    documentos: data || [],
    isLoading,
    isError,
    refetch,
  };
}
