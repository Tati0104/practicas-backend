import { useMemo } from 'react';
import { FiltrosActivos } from '@/shared/components/filtros';

const ACTIVO = [
  { value: 'true', label: 'Activas' },
  { value: 'false', label: 'Inactivas' },
];

export default function FiltrosEmpresa({ filtros, onChange }) {
  const campos = useMemo(
    () => [
      {
        key: 'busqueda',
        label: 'Búsqueda',
        type: 'text',
        placeholder: 'Buscar por nombre o NIT…',
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
