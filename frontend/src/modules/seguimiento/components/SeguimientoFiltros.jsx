// src/modules/seguimiento/components/SeguimientoFiltros.jsx

/**
 * Barra de filtros del tablero de seguimiento.
 * Filtros: búsqueda libre, estado (Al día / Pendiente / En alerta).
 */
export default function SeguimientoFiltros({ filtros, setFiltros }) {
  const actualizar = (campo, valor) =>
    setFiltros((f) => ({ ...f, [campo]: valor, page: 0 }));

  const limpiar = () =>
    setFiltros((f) => ({ ...f, busqueda: '', estado: '', programaId: '', docenteId: '', page: 0 }));

  return (
    <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10, marginBottom: 16 }}>
      <input
        placeholder="Buscar estudiante, empresa, cargo..."
        value={filtros.busqueda}
        onChange={(e) => actualizar('busqueda', e.target.value)}
        style={estilos.input}
      />
      <select
        value={filtros.estado}
        onChange={(e) => actualizar('estado', e.target.value)}
        style={estilos.select}
      >
        <option value="">Todos los estados</option>
        <option value="AL_DIA">Al día</option>
        <option value="PENDIENTE">Pendiente</option>
        <option value="EN_ALERTA">En alerta</option>
      </select>
      <button onClick={limpiar} style={estilos.btnLimpiar}>Limpiar</button>
    </div>
  );
}

const estilos = {
  input: { padding: '8px 12px', border: '1px solid #d1d5db', borderRadius: 8, fontSize: 14, minWidth: 260, flex: 1 },
  select: { padding: '8px 12px', border: '1px solid #d1d5db', borderRadius: 8, fontSize: 14, background: '#fff' },
  btnLimpiar: { padding: '8px 16px', border: '1px solid #d1d5db', borderRadius: 8, background: '#fff', fontSize: 14, cursor: 'pointer', color: '#374151' },
};
