const APTITUDES = ['SIN_EVALUAR', 'APTO', 'NO_APTO'];

export default function FiltrosEstudiante({ filtros, onChange }) {
  return (
    <div style={estilos.contenedor}>
      <input
        placeholder="Buscar por nombre o ID..."
        value={filtros.busqueda || ''}
        onChange={e => onChange({ ...filtros, busqueda: e.target.value || undefined })}
        style={estilos.input}
      />
      <select
        value={filtros.estadoAptitud || ''}
        onChange={e => onChange({ ...filtros, estadoAptitud: e.target.value || undefined })}
        style={estilos.select}
      >
        <option value="">Todos los estados</option>
        {APTITUDES.map(a => <option key={a} value={a}>{a}</option>)}
      </select>
      <select
        value={filtros.activo ?? ''}
        onChange={e => onChange({ ...filtros, activo: e.target.value === '' ? undefined : e.target.value === 'true' })}
        style={estilos.select}
      >
        <option value="">Activo / Inactivo</option>
        <option value="true">Activos</option>
        <option value="false">Inactivos</option>
      </select>
    </div>
  );
}

const estilos = {
  contenedor: { display: 'flex', gap: 10, marginBottom: 16, flexWrap: 'wrap' },
  input:  { padding: '8px 12px', border: '1px solid #d1d5db', borderRadius: 8, fontSize: 13, minWidth: 220 },
  select: { padding: '8px 12px', border: '1px solid #d1d5db', borderRadius: 8, fontSize: 13, background: '#fff', cursor: 'pointer' }
};