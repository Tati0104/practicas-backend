// src/modules/asignaciones/components/HistorialEstados.jsx

/**
 * Línea de tiempo del historial de estados de una asignación.
 * Recibe `historial`: array de { estado, fecha, observacion }
 * Si no hay historial, muestra un mensaje vacío.
 */
import BadgeAsignacion from './BadgeAsignacion';

export default function HistorialEstados({ historial = [] }) {
  if (historial.length === 0) {
    return (
      <div style={{ color: '#9ca3af', fontSize: 13, padding: '12px 0' }}>
        Sin historial de estados registrado.
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 0 }}>
      {historial.map((item, i) => (
        <div key={i} style={estilos.item}>
          {/* Línea vertical de la timeline */}
          <div style={estilos.lineaCol}>
            <div style={estilos.punto} />
            {i < historial.length - 1 && <div style={estilos.linea} />}
          </div>

          {/* Contenido del evento */}
          <div style={estilos.contenido}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
              <BadgeAsignacion estado={item.estado} />
              <span style={estilos.fecha}>
                {item.fecha
                  ? new Date(item.fecha).toLocaleDateString('es-CO', {
                      day: '2-digit', month: 'short', year: 'numeric',
                    })
                  : '—'}
              </span>
            </div>
            {item.observacion && (
              <p style={estilos.observacion}>{item.observacion}</p>
            )}
          </div>
        </div>
      ))}
    </div>
  );
}

const estilos = {
  item: { display: 'flex', gap: 12, paddingBottom: 16 },
  lineaCol: { display: 'flex', flexDirection: 'column', alignItems: 'center', width: 16 },
  punto: {
    width: 12, height: 12, borderRadius: '50%',
    background: '#2563eb', flexShrink: 0, marginTop: 4,
  },
  linea: { width: 2, flex: 1, background: '#dbeafe', marginTop: 2 },
  contenido: { flex: 1 },
  fecha: { fontSize: 11, color: '#6b7280' },
  observacion: { fontSize: 12, color: '#374151', margin: 0, marginTop: 2, fontStyle: 'italic' },
};
