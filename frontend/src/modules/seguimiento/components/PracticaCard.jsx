// src/modules/seguimiento/components/PracticaCard.jsx

/**
 * Tarjeta de una práctica en el tablero de seguimiento (vista mobile).
 */
import { useNavigate } from 'react-router-dom';

const BADGE = {
  AL_DIA:    { label: 'Al día',    bg: '#dcfce7', color: '#15803d' },
  PENDIENTE: { label: 'Pendiente', bg: '#fef3c7', color: '#b45309' },
  EN_ALERTA: { label: 'En alerta', bg: '#fee2e2', color: '#b91c1c' },
};

export default function PracticaCard({ practica, onVerDetalle }) {
  const badge = BADGE[practica.estado] || { label: practica.estado, bg: '#f3f4f6', color: '#374151' };

  return (
    <div style={{ background: '#fff', border: '1px solid #e5e7eb', borderRadius: 10, padding: '14px 16px', display: 'flex', flexDirection: 'column', gap: 8 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div>
          <div style={{ fontWeight: 700, fontSize: 15, color: '#111827' }}>{practica.estudiante?.nombre}</div>
          <div style={{ fontSize: 12, color: '#6b7280' }}>{practica.estudiante?.codigo} — {practica.estudiante?.programa}</div>
        </div>
        <span style={{ background: badge.bg, color: badge.color, borderRadius: 20, padding: '2px 10px', fontSize: 12, fontWeight: 600 }}>
          {badge.label}
        </span>
      </div>
      <div style={{ fontSize: 13, color: '#374151' }}>
        <span style={{ fontWeight: 600 }}>{practica.cargo}</span> · {practica.empresa}
      </div>
      <div style={{ fontSize: 12, color: '#6b7280' }}>Docente: {practica.docente}</div>
      {/* Barra de avance */}
      <div style={{ background: '#e5e7eb', borderRadius: 99, height: 6, overflow: 'hidden' }}>
        <div style={{ background: '#2563eb', width: `${practica.porcentajeAvance || 0}%`, height: '100%', borderRadius: 99 }} />
      </div>
      <div style={{ fontSize: 11, color: '#6b7280', textAlign: 'right' }}>{practica.porcentajeAvance || 0}% completado</div>
      <button onClick={() => onVerDetalle(practica.id)} style={{ padding: '7px 14px', background: '#2563eb', color: '#fff', border: 'none', borderRadius: 7, fontSize: 13, cursor: 'pointer', fontWeight: 600 }}>
        Ver detalle
      </button>
    </div>
  );
}
