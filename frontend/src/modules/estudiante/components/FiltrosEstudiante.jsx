import { useMemo } from 'react';
import { FiltrosActivos } from '@/shared/components/filtros';

const APTITUDES = [
  { value: 'SIN_EVALUAR', label: 'Sin evaluar' },
  { value: 'APTO', label: 'Apto' },
  { value: 'NO_APTO', label: 'No apto' },
];

const ACTIVO = [
  { value: 'true', label: 'Activos' },
  { value: 'false', label: 'Inactivos' },
];

export default function FiltrosEstudiante({ filtros, onChange }) {
  const campos = useMemo(
    () => [
      {
        key: 'busqueda',
        label: 'Búsqueda',
        type: 'text',
        placeholder: 'Nombre o documento…',
      },
      {
        key: 'estadoAptitud',
        label: 'Apto',
        type: 'select',
        opciones: APTITUDES,
      },
      {
        key: 'activo',
        label: 'Estado',
        type: 'select',
        opciones: ACTIVO,
      },
    ],
    []
  );

  const filtrosNormalizados = {
    ...filtros,
    activo:
      filtros.activo === true || filtros.activo === false
        ? String(filtros.activo)
        : filtros.activo ?? '',
  };

  const handleChange = (nuevos) => {
    const activo =
      nuevos.activo === '' || nuevos.activo === undefined
        ? undefined
        : nuevos.activo === 'true' || nuevos.activo === true;
    onChange({ ...nuevos, activo, page: 0 });
  };

  return (
    <FiltrosActivos
      campos={campos}
      filtros={filtrosNormalizados}
      onChange={handleChange}
    />
  );
}
