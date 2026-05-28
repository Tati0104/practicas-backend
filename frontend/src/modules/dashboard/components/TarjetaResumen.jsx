export default function TarjetaResumen({ titulo, valor, icono, color = '#1e3a5f' }) {
    return (
      <div style={{ ...estilos.tarjeta, borderTop: `4px solid ${color}` }}>
        <div style={estilos.fila}>
          <div>
            <p style={estilos.titulo}>{titulo}</p>
            <p style={{ ...estilos.valor, color }}>{valor ?? '—'}</p>
          </div>
          <span style={{ ...estilos.icono, background: color + '20', color }}>
            {icono}
          </span>
        </div>
      </div>
    );
  }
  
  const estilos = {
    tarjeta: {
      background: '#fff',
      borderRadius: 10,
      padding: '20px 22px',
      boxShadow: '0 1px 6px rgba(0,0,0,0.07)',
      fontFamily: 'Arial, sans-serif'
    },
    fila: {
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center'
    },
    titulo: {
      fontSize: 13,
      color: '#6b7280',
      margin: '0 0 6px'
    },
    valor: {
      fontSize: 28,
      fontWeight: 700,
      margin: 0
    },
    icono: {
      width: 48,
      height: 48,
      borderRadius: 10,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      fontSize: 22
    }
  };