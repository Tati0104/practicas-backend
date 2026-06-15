import { useState, useRef, useEffect } from 'react';
import { useAlertas } from '../../modules/seguimiento/hooks/useAlertas';

export default function NotificacionesBell() {
  const [abierto, setAbierto] = useState(false);
  const ref = useRef(null);
  const { alertas, noLeidas, isLoading, soloNoLeidas, setSoloNoLeidas, marcarLeida, isPendingMarcar } =
    useAlertas();

  useEffect(() => {
    const handler = (e) => { if (ref.current && !ref.current.contains(e.target)) setAbierto(false); };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  return (
    <div ref={ref} style={{ position: 'relative' }}>
      <button onClick={() => setAbierto(v => !v)} style={estilos.btn} title="Notificaciones">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor"
          strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
          <path d="M13.73 21a2 2 0 0 1-3.46 0" />
        </svg>
        {noLeidas > 0 && (
          <span style={estilos.badge}>{noLeidas > 99 ? '99+' : noLeidas}</span>
        )}
      </button>

      {abierto && (
        <div style={estilos.dropdown}>
          <div style={estilos.header}>
            <span style={estilos.titulo}>
              Notificaciones {noLeidas > 0 && <span style={estilos.badgeInline}>{noLeidas}</span>}
            </span>
            <button onClick={() => setSoloNoLeidas(!soloNoLeidas)} style={estilos.filtroBtn}>
              {soloNoLeidas ? 'Ver todas' : 'Solo no leídas'}
            </button>
          </div>

          <div style={estilos.lista}>
            {isLoading && <p style={estilos.msg}>Cargando...</p>}
            {!isLoading && alertas.length === 0 && (
              <p style={estilos.msg}>Sin alertas {soloNoLeidas ? 'pendientes' : ''} ✓</p>
            )}
            {alertas.map(a => (
              <div key={a.id} style={{ ...estilos.item, background: a.leida ? '#f9fafb' : '#fef3c7', borderColor: a.leida ? '#e5e7eb' : '#fcd34d' }}>
                <p style={estilos.itemMsg}>{a.mensaje}</p>
                <div style={estilos.itemMeta}>
                  <span style={estilos.itemFecha}>{a.fecha}</span>
                  {!a.leida && (
                    <button onClick={() => marcarLeida(a.id)} disabled={isPendingMarcar} style={estilos.leidaBtn}>
                      Marcar leída
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

const estilos = {
  btn: {
    position: 'relative', background: 'none', border: 'none', cursor: 'pointer',
    color: '#6b7280', padding: '6px', borderRadius: 8, display: 'flex', alignItems: 'center',
    transition: 'color 0.15s',
  },
  badge: {
    position: 'absolute', top: 1, right: 1,
    background: '#ef4444', color: '#fff', fontSize: 9, fontWeight: 700,
    borderRadius: 99, minWidth: 16, height: 16, display: 'flex', alignItems: 'center',
    justifyContent: 'center', padding: '0 3px', lineHeight: 1,
  },
  dropdown: {
    position: 'absolute', right: 0, top: 'calc(100% + 8px)',
    width: 320, background: '#fff', borderRadius: 10,
    boxShadow: '0 8px 24px rgba(0,0,0,0.12)', border: '1px solid #e5e7eb',
    zIndex: 1000, overflow: 'hidden',
  },
  header: {
    display: 'flex', justifyContent: 'space-between', alignItems: 'center',
    padding: '12px 16px', borderBottom: '1px solid #e5e7eb',
  },
  titulo: { fontSize: 14, fontWeight: 700, color: '#111827', display: 'flex', alignItems: 'center', gap: 6 },
  badgeInline: {
    background: '#ef4444', color: '#fff', fontSize: 11, fontWeight: 700,
    borderRadius: 99, padding: '1px 6px',
  },
  filtroBtn: {
    fontSize: 12, color: '#2563eb', background: 'none', border: 'none',
    cursor: 'pointer', fontWeight: 600,
  },
  lista: { maxHeight: 360, overflowY: 'auto', padding: '8px 0' },
  msg: { color: '#9ca3af', fontSize: 13, textAlign: 'center', padding: '20px 0', margin: 0 },
  item: {
    margin: '4px 12px', padding: '10px 12px', borderRadius: 8,
    border: '1px solid', display: 'flex', flexDirection: 'column', gap: 4,
  },
  itemMsg: { fontSize: 13, color: '#374151', margin: 0 },
  itemMeta: { display: 'flex', justifyContent: 'space-between', alignItems: 'center' },
  itemFecha: { fontSize: 11, color: '#9ca3af' },
  leidaBtn: {
    fontSize: 11, color: '#2563eb', background: 'none', border: 'none',
    cursor: 'pointer', fontWeight: 600,
  },
};