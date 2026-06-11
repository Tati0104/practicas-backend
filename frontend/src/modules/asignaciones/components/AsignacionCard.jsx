// src/modules/asignaciones/components/AsignacionCard.jsx

/**
 * Tarjeta individual de asignación para vista móvil.
 * Muestra los mismos datos que la tabla pero en formato card apilado.
 * Se usa cuando el ancho de pantalla es < 1280px.
 */
import { useNavigate } from 'react-router-dom';
import BadgeAsignacion from './BadgeAsignacion';

export default function AsignacionCard({ asignacion, onCancelar, canCancelar }) {
  const navigate = useNavigate();

  return (
    <div style={estilos.card}>
      {/* Encabezado: nombre del estudiante + badge de estado */}
      <div style={estilos.header}>
        <div>
          <div style={estilos.nombre}>{asignacion.estudiante?.nombre || '—'}</div>
          <div style={estilos.subInfo}>
            {asignacion.estudiante?.codigo} · {asignacion.estudiante?.programa}
          </div>
        </div>
        <BadgeAsignacion estado={asignacion.estado} />
      </div>

      {/* Datos de la vacante */}
      <div style={estilos.fila}>
        <span style={estilos.label}>Cargo:</span>
        <span style={estilos.valor}>{asignacion.vacante?.cargo || '—'}</span>
      </div>
      <div style={estilos.fila}>
        <span style={estilos.label}>Empresa:</span>
        <span style={estilos.valor}>{asignacion.vacante?.empresa || '—'}</span>
      </div>
      <div style={estilos.fila}>
        <span style={estilos.label}>Fecha:</span>
        <span style={estilos.valor}>
          {asignacion.fechaAsignacion
            ? new Date(asignacion.fechaAsignacion).toLocaleDateString('es-CO')
            : '—'}
        </span>
      </div>

      {/* Botones de acción */}
      <div style={estilos.acciones}>
        <button
          onClick={() => navigate(`/asignaciones/${asignacion.id}`)}
          style={estilos.btnVer}
        >
          Ver detalle
        </button>

        {canCancelar &&
          asignacion.estado !== 'CANCELADA' &&
          asignacion.estado !== 'VINCULADA' && (
            <button
              onClick={() => onCancelar(asignacion)}
              style={estilos.btnCancelar}
            >
              Cancelar
            </button>
          )}
      </div>
    </div>
  );
}

const estilos = {
  card: {
    background: '#ffffff',
    border: '1px solid #e5e7eb',
    borderRadius: 10,
    padding: 16,
    display: 'flex',
    flexDirection: 'column',
    gap: 8,
    boxShadow: '0 1px 3px rgba(0,0,0,0.06)',
  },
  header: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 4,
  },
  nombre:  { fontWeight: 700, fontSize: 14, color: '#111827' },
  subInfo: { fontSize: 11, color: '#6b7280', marginTop: 2 },
  fila:    { display: 'flex', gap: 8, fontSize: 13 },
  label:   { color: '#6b7280', minWidth: 65 },
  valor:   { color: '#111827', fontWeight: 500 },
  acciones: { display: 'flex', gap: 8, marginTop: 8 },
  btnVer: {
    flex: 1,
    padding: '7px 0',
    background: '#2563eb',
    color: '#fff',
    border: 'none',
    borderRadius: 6,
    fontSize: 13,
    cursor: 'pointer',
    fontWeight: 600,
  },
  btnCancelar: {
    flex: 1,
    padding: '7px 0',
    background: '#fee2e2',
    color: '#991b1b',
    border: '1px solid #fca5a5',
    borderRadius: 6,
    fontSize: 13,
    cursor: 'pointer',
    fontWeight: 600,
  },
};
