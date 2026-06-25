import { useMemo } from 'react';
import { FiltrosActivos } from '@/shared/components/filtros';
import { OPCIONES_ESTADO_PRACTICA } from '../utils/estadosPractica';

const ESTADOS = [
  { value: 'AL_DIA', label: 'Al día' },
  { value: 'PENDIENTE', label: 'Pendiente' },
  { value: 'EN_ALERTA', label: 'En alerta' },
];

export default function SeguimientoFiltros({ filtros, setFiltros, mostrarEstadoPractica = false }) {
  const campos = useMemo(() => {
    const base = [
      {
        key: 'busqueda',
        label: 'Búsqueda',
        type: 'text',
        placeholder: 'Buscar estudiante, empresa, cargo…',
      },
      {
        key: 'programaId',
        label: 'Programa',
        type: 'text',
        placeholder: 'ID del programa',
      },
      {
        key: 'estado',
        label: 'Estado de seguimiento',
        type: 'select',
        placeholder: 'Filtrar por seguimiento…',
        opciones: ESTADOS,
      },
    ];

    if (mostrarEstadoPractica) {
      base.splice(2, 0, {
        key: 'estadoPractica',
        label: 'Estado de práctica',
        type: 'select',
        placeholder: 'Todas las prácticas…',
        opciones: OPCIONES_ESTADO_PRACTICA,
      });
    }

    return base;
  }, [mostrarEstadoPractica]);

  return <FiltrosActivos campos={campos} filtros={filtros} onChange={setFiltros} />;
}
