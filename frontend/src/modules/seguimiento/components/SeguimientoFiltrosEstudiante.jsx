import { FiltrosBar, FiltroSelect } from '@/shared/components/filtros';
import { Button } from '@/shared/components/ui';

const ESTADOS = [
  { value: '', label: 'Todos los estados' },
  { value: 'AL_DIA', label: 'Al día / Revisado' },
  { value: 'PENDIENTE', label: 'Pendiente de revisión' },
  { value: 'EN_ALERTA', label: 'En alerta' },
];

export default function SeguimientoFiltrosEstudiante({ filtros, setFiltros, practicas = [] }) {
  const actualizar = (campo, valor) =>
    setFiltros((f) => ({ ...f, [campo]: valor, page: 0 }));

  const limpiar = () =>
    setFiltros((f) => ({
      ...f,
      numeroPractica: '',
      estado: '',
      page: 0,
    }));

  const opcionesPractica = [
    { value: '', label: 'Todas mis prácticas' },
    ...practicas
      .filter((p) => p.numeroPractica != null)
      .map((p) => ({
        value: String(p.numeroPractica),
        label: `Práctica ${p.numeroPractica}${p.empresa ? ` — ${p.empresa}` : ''}`,
      })),
  ];

  return (
    <FiltrosBar variant="inline">
      <FiltroSelect
        compacto
        opciones={opcionesPractica}
        value={filtros.numeroPractica ?? ''}
        onChange={(e) => actualizar('numeroPractica', e.target.value)}
      />
      <FiltroSelect
        compacto
        opciones={ESTADOS}
        value={filtros.estado}
        onChange={(e) => actualizar('estado', e.target.value)}
      />
      <Button type="button" variant="secondary" size="sm" onClick={limpiar}>
        Limpiar
      </Button>
    </FiltrosBar>
  );
}
