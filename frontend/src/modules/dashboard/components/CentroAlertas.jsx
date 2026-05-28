import { useAlertas } from '../hooks/useDashboard';

export default function CentroAlertas() {
  const { alertas, marcarLeida } = useAlertas();

  if (alertas.isLoading) return <p style={estilos.msg}>Cargando alertas...</p>;
  if (alertas.isError)   return <p style={estilos.msg}>No se pudieron cargar las alertas.</p>;

  const lista = alertas.data?.content || [];

  return (
    <div style={estilos.contenedor}>
      <h3 style={estilos.titulo}>🔔 Alertas y notificaciones</h3>
      {lista.length === 0 ? (
        <p style={estilos.sinAlertas}>No tienes alertas pendientes ✓</p>
      ) : (
        <div style={estilos.lista}>
          {lista.map(alerta => (
            <div key={alerta.id} style={estilos.item}>
              <div style={estilos.itemTexto}>
                <span style={estilos.itemTipo}>{alerta.tipo}</span>
                <p style={estilos.itemMsg}>{alerta.mensaje}</p>
              </div>
              {!alerta.leida && (
                <button
                  onClick={() => marcarLeida.mutate(alerta.id)}
                  style={estilos.btnLeida}
                >
                  Marcar leída
                </button>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

const estilos = {
  contenedor: {
    background: '#fff',
    borderRadius: 10,
    padding: '20px 22px',
    boxShadow: '0 1px 6px rgba(0,0,0,0.07)',
    fontFamily: 'Arial, sans-serif'
  },
  titulo: { fontSize: 15, fontWeight: 600, color: '#1e3a5f', margin: '0 0 14px' },
  msg:    { color: '#6b7280', fontSize: 13 },
  sinAlertas: { color: '#059669', fontSize: 13, margin: 0 },
  lista:  { display: 'flex', flexDirection: 'column', gap: 8 },
  item:   {
    display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start',
    padding: '10px 12px', background: '#f8fafc',
    borderRadius: 8, border: '1px solid #e5e7eb'
  },
  itemTexto: { flex: 1 },
  itemTipo:  { fontSize: 11, fontWeight: 600, color: '#1e40af', textTransform: 'uppercase' },
  itemMsg:   { fontSize: 13, color: '#374151', margin: '2px 0 0' },
  btnLeida:  {
    fontSize: 11, padding: '4px 8px', background: '#dbeafe',
    color: '#1e40af', border: 'none', borderRadius: 6, cursor: 'pointer'
  }
};