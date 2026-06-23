import { useMemo } from 'react';
import { FiltrosActivos } from '@/shared/components/filtros';

const ESTADOS = [
  { value: 'AL_DIA', label: 'Al día / Revisado' },
  { value: 'PENDIENTE', label: 'Pendiente de revisión' },
  { value: 'EN_ALERTA', label: 'En alerta' },
];

export default function SeguimientoFiltrosEstudiante({ filtros, setFiltros, practicas = [] }) {
  const campos = useMemo(() => {
    const opcionesPractica = practicas
      .filter((p) => p.numeroPractica != null)
      .map((p) => ({
        value: String(p.numeroPractica),
        label: `Práctica ${p.numeroPractica}${p.empresa ? ` — ${p.empresa}` : ''}`,
      }));

    return [
      {
        key: 'numeroPractica',
        label: 'Práctica',
        type: 'select',
        placeholder: 'Todas mis prácticas…',
        opciones: opcionesPractica,
      },
      {
        key: 'estado',
        label: 'Estado de seguimiento',
        type: 'select',
        placeholder: 'Filtrar por estado…',
        opciones: ESTADOS,
      },
    ];
  }, [practicas]);

  return <FiltrosActivos campos={campos} filtros={filtros} onChange={setFiltros} />;
}
