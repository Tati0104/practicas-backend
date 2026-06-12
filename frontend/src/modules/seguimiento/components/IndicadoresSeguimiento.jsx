// src/modules/seguimiento/components/IndicadoresSeguimiento.jsx

/**
 * Tarjetas de resumen del tablero de seguimiento.
 * Muestra: Al día, Pendiente, En alerta — calculado desde la lista recibida.
 */
export default function IndicadoresSeguimiento({ practicas = [] }) {
  const alDia     = practicas.filter((p) => p.estado === 'AL_DIA').length;
  const pendiente = practicas.filter((p) => p.estado === 'PENDIENTE').length;
  const enAlerta  = practicas.filter((p) => p.estado === 'EN_ALERTA').length;

  const tarjetas = [
    { titulo: 'Al día',     valor: alDia,     color: '#15803d', fondo: '#dcfce7', icono: '✅' },
    { titulo: 'Pendiente',  valor: pendiente, color: '#b45309', fondo: '#fef3c7', icono: '⏳' },
    { titulo: 'En alerta',  valor: enAlerta,  color: '#b91c1c', fondo: '#fee2e2', icono: '🚨' },
  ];

  return (
    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: 12, marginBottom: 20 }}>
      {tarjetas.map((t) => (
        <div key={t.titulo} style={{ background: t.fondo, border: `1px solid ${t.color}22`, borderRadius: 10, padding: '14px 18px' }}>
          <div style={{ fontSize: 20 }}>{t.icono}</div>
          <div style={{ fontSize: 28, fontWeight: 800, color: t.color }}>{t.valor}</div>
          <div style={{ fontSize: 12, color: '#374151', fontWeight: 600 }}>{t.titulo}</div>
        </div>
      ))}
    </div>
  );
}
