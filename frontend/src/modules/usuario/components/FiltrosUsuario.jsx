import { useMemo } from 'react';
import { FiltrosActivos } from '@/shared/components/filtros';
import { opcionesRol } from '../constants/catalogoUsuario';

const ACTIVO = [
  { value: 'true', label: 'Activos' },
  { value: 'false', label: 'Inactivos' },
];

export default function FiltrosUsuario({ filtros, onChange }) {
  const campos = useMemo(
    () => [
      {
        key: 'rol',
        label: 'Rol',
        type: 'select',
        opciones: opcionesRol().map((r) => ({ value: r.value, label: r.label })),
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
