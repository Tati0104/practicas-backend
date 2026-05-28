import { useState } from 'react';
import { useVacantes } from '../hooks/useEmpresas';
import TablaBase from '../../../shared/components/TablaBase';

const colorEstado = {
  ACTIVA:               { bg: '#d1fae5', color: '#065f46' },
  PENDIENTE_APROBACION: { bg: '#fef3c7', color: '#92400e' },
  PAUSADA:              { bg: '#f3f4f6', color: '#374151' },
  CUPOS_COMPLETOS:      { bg: '#fed7aa', color: '#92400e' },
  CERRADA:              { bg: '#fee2e2', color: '#991b1b' },
};

export default function VacantesPage() {
  const { vacantes, isLoading, aprobar, rechazar } = useVacantes();
  const [motivoModal, setMotivoModal] = useState(null);
  const [motivo,      setMotivo]      = useState('');

  const columnas = [
    { key: 'empresa',   titulo: 'Empresa' },
    { key: 'cargo',     titulo: 'Cargo' },
    { key: 'modalidad', titulo: 'Modalidad' },
    {
      key: 'cupos', titulo: 'Cupos',
      render: v => `${v.cuposDisponibles} / ${v.cuposTotal}`
    },
    {
      key: 'estado', titulo: 'Estado',
      render: v => {
        const c = colorEstado[v.estado] || colorEstado.CERRADA;
        return (
          <span style={{ fontSize: 11, fontWeight: 600, padding: '3px 10px',
            borderRadius: 20, background: c.bg, color: c.color }}>
            {v.estado.replace('_', ' ')}
          </span>
        );
      }
    },
    {
      key: 'acciones', titulo: 'Acciones',
      render: v => v.estado === 'PENDIENTE_APROBACION' ? (
        <div style={{ display: 'flex', gap: 6 }}>
          <button onClick={() => aprobar.mutate(v.id)}
            style={{ padding: '4px 10px', background: '#d1fae5', color: '#065f46', border: 'none', borderRadius: 6, cursor: 'pointer', fontSize: 11, fontWeight: 600 }}>
            ✓ Aprobar
          </button>
          <button onClick={() => { setMotivoModal(v.id); setMotivo(''); }}
            style={{ padding: '4px 10px', background: '#fee2e2', color: '#991b1b', border: 'none', borderRadius: 6, cursor: 'pointer', fontSize: 11, fontWeight: 600 }}>
            ✗ Rechazar
          </button>
        </div>
      ) : null
    }
  ];

  return (
    <div style={{ fontFamily: 'Arial, sans-serif' }}>
      <h2 style={{ fontSize: 20, fontWeight: 700, color: '#1e3a5f', marginBottom: 16 }}>
        Vacantes
      </h2>
      <TablaBase columnas={columnas} datos={vacantes} cargando={isLoading} />

      {motivoModal && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.45)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 200 }}>
          <div style={{ background: '#fff', borderRadius: 12, padding: 24, width: 380, display: 'flex', flexDirection: 'column', gap: 12 }}>
            <h3 style={{ fontSize: 16, fontWeight: 700, color: '#991b1b', margin: 0 }}>Rechazar vacante</h3>
            <label style={{ fontSize: 13, fontWeight: 600, color: '#374151' }}>Motivo (obligatorio)</label>
            <textarea value={motivo} onChange={e => setMotivo(e.target.value)} rows={3}
              placeholder="Explica el motivo del rechazo..."
              style={{ padding: '9px 11px', border: '1px solid #d1d5db', borderRadius: 8, fontSize: 13, resize: 'vertical' }} />
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
              <button onClick={() => setMotivoModal(null)}
                style={{ padding: '8px 16px', background: '#f3f4f6', color: '#374151', border: 'none', borderRadius: 8, cursor: 'pointer' }}>
                Cancelar
              </button>
              <button
                onClick={() => { rechazar.mutate({ id: motivoModal, motivo }); setMotivoModal(null); }}
                disabled={!motivo.trim()}
                style={{ padding: '8px 16px', background: motivo.trim() ? '#991b1b' : '#9ca3af', color: '#fff', border: 'none', borderRadius: 8, cursor: motivo.trim() ? 'pointer' : 'not-allowed' }}>
                Rechazar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}