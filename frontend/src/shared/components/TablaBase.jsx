export default function TablaBase({ columnas, datos, cargando, sinDatos = 'No hay registros' }) {
    if (cargando) return <p style={{ color: '#6b7280', fontSize: 13 }}>Cargando...</p>;
  
    return (
      <div style={estilos.contenedor}>
        <table style={estilos.tabla}>
          <thead>
            <tr>
              {columnas.map(col => (
                <th key={col.key} style={estilos.th}>{col.titulo}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {datos.length === 0 ? (
              <tr>
                <td colSpan={columnas.length} style={estilos.sinDatos}>
                  {sinDatos}
                </td>
              </tr>
            ) : (
              datos.map((fila, i) => (
                <tr key={i} style={i % 2 === 0 ? estilos.filaParC : estilos.filaImpar}>
                  {columnas.map(col => (
                    <td key={col.key} style={estilos.td}>
                      {col.render ? col.render(fila) : fila[col.key]}
                    </td>
                  ))}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    );
  }
  
  const estilos = {
    contenedor: { overflowX: 'auto', borderRadius: 8, border: '1px solid #e5e7eb' },
    tabla:   { width: '100%', borderCollapse: 'collapse', fontFamily: 'Arial, sans-serif' },
    th:      { padding: '11px 14px', background: '#f8fafc', color: '#374151', fontSize: 12, fontWeight: 600, textAlign: 'left', borderBottom: '1px solid #e5e7eb' },
    td:      { padding: '10px 14px', fontSize: 13, color: '#374151', borderBottom: '1px solid #f1f5f9' },
    filaParC:  { background: '#ffffff' },
    filaImpar: { background: '#fafafa' },
    sinDatos:  { padding: 24, textAlign: 'center', color: '#9ca3af', fontSize: 13 }
  };