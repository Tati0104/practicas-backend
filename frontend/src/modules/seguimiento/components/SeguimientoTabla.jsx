// src/modules/seguimiento/components/SeguimientoTabla.jsx

/**
 * Tabla del tablero de seguimiento (vista desktop).
 */
import { useNavigate } from 'react-router-dom';

const BADGE = {
  AL_DIA:    { label: 'Al día',    bg: '#dcfce7', color: '#15803d' },
  PENDIENTE: { label: 'Pendiente', bg: '#fef3c7', color: '#b45309' },
  EN_ALERTA: { label: 'En alerta', bg: '#fee2e2', color: '#b91c1c' },
};

export default function SeguimientoTabla({ practicas = [], onVerDetalle }) {
  return (
    <div style={{ overflowX: 'auto', borderRadius: 10, border: '1px solid #e5e7eb' }}>
      <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 14 }}>
        <thead>
          <tr style={{ background: '#f9fafb', borderBottom: '1px solid #e5e7eb' }}>
            {['Estudiante', 'Empresa / Cargo', 'Docente', 'Avance', 'Estado', 'Acciones'].map((h) => (
              <th key={h} style={{ padding: '10px 14px', textAlign: 'left', fontWeight: 700, color: '#374151', fontSize: 13 }}>{h}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {practicas.map((p) => {
            const badge = BADGE[p.estado] || { label: p.estado, bg: '#f3f4f6', color: '#374151' };
            return (
              <tr key={p.id} style={{ borderBottom: '1px solid #f3f4f6' }}>
                <td style={{ padding: '12px 14px' }}>
                  <div style={{ fontWeight: 600, color: '#111827' }}>{p.estudiante?.nombre}</div>
                  <div style={{ fontSize: 12, color: '#6b7280' }}>{p.estudiante?.codigo}</div>
                </td>
                <td style={{ padding: '12px 14px' }}>
                  <div style={{ fontWeight: 600 }}>{p.cargo}</div>
                  <div style={{ fontSize: 12, color: '#6b7280' }}>{p.empresa}</div>
                </td>
                <td style={{ padding: '12px 14px', color: '#374151' }}>{p.docente}</td>
                <td style={{ padding: '12px 14px', minWidth: 120 }}>
                  <div style={{ background: '#e5e7eb', borderRadius: 99, height: 6, overflow: 'hidden', marginBottom: 4 }}>
                    <div style={{ background: '#2563eb', width: `${p.porcentajeAvance || 0}%`, height: '100%', borderRadius: 99 }} />
                  </div>
                  <div style={{ fontSize: 11, color: '#6b7280' }}>{p.porcentajeAvance || 0}%</div>
                </td>
                <td style={{ padding: '12px 14px' }}>
                  <span style={{ background: badge.bg, color: badge.color, borderRadius: 20, padding: '3px 10px', fontSize: 12, fontWeight: 600 }}>
                    {badge.label}
                  </span>
                </td>
                <td style={{ padding: '12px 14px' }}>
                  <button
                    onClick={() => onVerDetalle(p.id)}
                    style={{ padding: '6px 14px', background: '#2563eb', color: '#fff', border: 'none', borderRadius: 7, fontSize: 13, cursor: 'pointer', fontWeight: 600 }}
                  >
                    Ver
                  </button>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
