import { FiltrosBar, FiltroInput, FiltroSelect, estilosFiltros } from '@/shared/components/filtros';

const ESTADOS = [
  { value: '', label: 'Todos los estados' },
  { value: 'AL_DIA', label: 'Al día' },
  { value: 'PENDIENTE', label: 'Pendiente' },
  { value: 'EN_ALERTA', label: 'En alerta' },
];

export default function SeguimientoFiltros({ filtros, setFiltros }) {
  const actualizar = (campo, valor) =>
    setFiltros((f) => ({ ...f, [campo]: valor, page: 0 }));

  const limpiar = () =>
    setFiltros((f) => ({
      ...f,
      busqueda: '',
      estado: '',
      docenteId: '',
      page: 0,
    }));

  return (
    <FiltrosBar variant="inline">
      <FiltroInput
        compacto
        placeholder="Buscar estudiante, empresa, cargo..."
        value={filtros.busqueda}
        onChange={(e) => actualizar('busqueda', e.target.value)}
      />
      <FiltroInput
        compacto
        placeholder="ID Programa"
        value={filtros.programaId}
        onChange={(e) => actualizar('programaId', e.target.value)}
        style={{ maxWidth: 130 }}
      />
      <FiltroSelect
        compacto
        opciones={ESTADOS}
        value={filtros.estado}
        onChange={(e) => actualizar('estado', e.target.value)}
      />
      <button type="button" onClick={limpiar} style={estilosFiltros.btnLimpiar}>
        Limpiar
      </button>
    </FiltrosBar>
  );
}
