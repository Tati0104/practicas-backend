// src/modules/seguimiento/components/TimelineSeguimiento.jsx

/**
 * Timeline de eventos de una práctica.
 * Ordena del más reciente al más antiguo (el hook ya lo ordena, pero aquí se renderiza).
 * Tipos: OBSERVACION, AVANCE_TUTOR, BITACORA.
 */
const TIPO_CONFIG = {
  OBSERVACION:  { icono: '👁️', label: 'Observación',   color: '#2563eb', fondo: '#eff6ff' },
  AVANCE_TUTOR: { icono: '📈', label: 'Avance tutor',  color: '#15803d', fondo: '#dcfce7' },
  BITACORA:     { icono: '📓', label: 'Bitácora',       color: '#7c3aed', fondo: '#ede9fe' },
};

export default function TimelineSeguimiento({ timeline = [] }) {
  if (timeline.length === 0) {
    return (
      <div style={{ textAlign: 'center', padding: '30px 0', color: '#9ca3af', fontSize: 14 }}>
        Sin eventos registrados aún.
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 0 }}>
      {timeline.map((evento, idx) => {
        const cfg = TIPO_CONFIG[evento.tipo] || { icono: '📌', label: evento.tipo, color: '#374151', fondo: '#f3f4f6' };
        return (
          <div key={evento.id} style={{ display: 'flex', gap: 14 }}>
            {/* Línea vertical */}
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
              <div style={{ width: 36, height: 36, borderRadius: '50%', background: cfg.fondo, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 16, flexShrink: 0, border: `2px solid ${cfg.color}33` }}>
                {cfg.icono}
              </div>
              {idx < timeline.length - 1 && (
                <div style={{ width: 2, flex: 1, background: '#e5e7eb', margin: '4px 0' }} />
              )}
            </div>
            {/* Contenido */}
            <div style={{ flex: 1, paddingBottom: 20 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 4 }}>
                <span style={{ background: cfg.fondo, color: cfg.color, borderRadius: 20, padding: '2px 10px', fontSize: 12, fontWeight: 600 }}>
                  {cfg.label}
                </span>
                <span style={{ fontSize: 12, color: '#9ca3af' }}>{evento.fecha}</span>
              </div>
              <div style={{ fontSize: 14, color: '#374151', marginBottom: 4 }}>{evento.contenido}</div>
              {evento.porcentaje !== undefined && (
                <div style={{ fontSize: 12, color: '#6b7280' }}>Avance: {evento.porcentaje}%</div>
              )}
              <div style={{ fontSize: 12, color: '#9ca3af' }}>Por: {evento.autor}</div>
            </div>
          </div>
        );
      })}
    </div>
  );
}
