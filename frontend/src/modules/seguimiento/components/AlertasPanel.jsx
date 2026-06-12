// src/modules/seguimiento/components/AlertasPanel.jsx

/**
 * Panel lateral de alertas.
 * Permite filtrar por no leídas y marcar alertas como leídas.
 */
import { useAlertas } from '../hooks/useAlertas';

export default function AlertasPanel({ practicaId = null }) {
  const { alertas, noLeidas, isLoading, soloNoLeidas, setSoloNoLeidas, marcarLeida, isPendingMarcar } =
    useAlertas(practicaId);

  return (
    <div style={{ background: '#fff', border: '1px solid #e5e7eb', borderRadius: 10, padding: 16, minWidth: 260 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
        <h3 style={{ margin: 0, fontSize: 15, fontWeight: 700, color: '#111827' }}>
          🔔 Alertas {noLeidas > 0 && <span style={{ background: '#ef4444', color: '#fff', borderRadius: 99, padding: '1px 7px', fontSize: 11, marginLeft: 6 }}>{noLeidas}</span>}
        </h3>
        <button
          onClick={() => setSoloNoLeidas(!soloNoLeidas)}
          style={{ fontSize: 12, color: soloNoLeidas ? '#2563eb' : '#6b7280', background: 'none', border: 'none', cursor: 'pointer', fontWeight: 600 }}
        >
          {soloNoLeidas ? 'Ver todas' : 'Solo no leídas'}
        </button>
      </div>

      {isLoading && <div style={{ color: '#9ca3af', fontSize: 13 }}>Cargando...</div>}

      {!isLoading && alertas.length === 0 && (
        <div style={{ color: '#9ca3af', fontSize: 13, textAlign: 'center', padding: '20px 0' }}>
          Sin alertas {soloNoLeidas ? 'pendientes' : ''}
        </div>
      )}

      <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
        {alertas.map((alerta) => (
          <div
            key={alerta.id}
            style={{
              background: alerta.leida ? '#f9fafb' : '#fef3c7',
              border: `1px solid ${alerta.leida ? '#e5e7eb' : '#fcd34d'}`,
              borderRadius: 8,
              padding: '10px 12px',
              display: 'flex',
              flexDirection: 'column',
              gap: 4,
            }}
          >
            <div style={{ fontSize: 13, color: '#374151' }}>{alerta.mensaje}</div>
            <div style={{ fontSize: 11, color: '#9ca3af' }}>{alerta.fecha}</div>
            {!alerta.leida && (
              <button
                onClick={() => marcarLeida(alerta.id)}
                disabled={isPendingMarcar}
                style={{ alignSelf: 'flex-end', fontSize: 11, color: '#2563eb', background: 'none', border: 'none', cursor: 'pointer', fontWeight: 600 }}
              >
                Marcar como leída
              </button>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
