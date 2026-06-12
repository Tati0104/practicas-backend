// src/modules/vinculacion/components/VinculacionCard.jsx

/**
 * Componente: VinculacionCard
 * ────────────────────────────
 * Tarjeta individual de vinculación para la vista MÓVIL (pantallas < 1280px).
 * Muestra la misma información que VinculacionTabla pero en formato apilado
 * para facilitar la lectura en pantallas pequeñas.
 *
 * ¿Qué muestra?
 *   - Nombre del estudiante + badge de estado de carta y convenio.
 *   - Código y programa del estudiante.
 *   - Cargo y empresa de la vacante.
 *   - Indicador de progreso: "X/2 firmados".
 *   - Botón "Gestionar" que navega al detalle de la práctica.
 *
 * Props:
 *   vinculacion  → object — datos de la vinculación con sus documentos
 *   onGestionar  → function(vinculacion) — navega al detalle
 */

import { useNavigate } from 'react-router-dom';
import BadgeDocumento from './BadgeDocumento';

export default function VinculacionCard({ vinculacion, onGestionar }) {
  const navigate = useNavigate();

  // Extraemos carta y convenio del array de documentos
  const carta    = vinculacion.documentos?.find((d) => d.tipo === 'CARTA');
  const convenio = vinculacion.documentos?.find((d) => d.tipo === 'CONVENIO');

  // Contamos cuántos documentos están completamente firmados
  const firmados = vinculacion.documentos?.filter((d) => d.estado === 'FIRMADO').length || 0;
  const total    = vinculacion.documentos?.length || 2;

  const irADetalle = () =>
    onGestionar
      ? onGestionar(vinculacion)
      : navigate(`/vinculacion/${vinculacion.practicaId}`);

  return (
    <div style={estilos.card}>

      {/* ── Encabezado: nombre del estudiante ── */}
      <div style={estilos.header}>
        <div>
          <div style={estilos.nombre}>
            {vinculacion.estudiante?.nombre || '—'}
          </div>
          <div style={estilos.subInfo}>
            {vinculacion.estudiante?.codigo} · {vinculacion.estudiante?.programa}
          </div>
        </div>

        {/* Progreso de documentos en el rincón derecho */}
        <span style={{
          ...estilos.progreso,
          color: firmados === total ? '#059669' : '#374151',
        }}>
          {firmados}/{total} ✓
        </span>
      </div>

      {/* ── Cargo y empresa ── */}
      <div style={estilos.fila}>
        <span style={estilos.label}>Cargo:</span>
        <span style={estilos.valor}>{vinculacion.vacante?.cargo || '—'}</span>
      </div>
      <div style={estilos.fila}>
        <span style={estilos.label}>Empresa:</span>
        <span style={estilos.valor}>{vinculacion.vacante?.empresa || '—'}</span>
      </div>

      {/* ── Estado de los documentos ── */}
      <div style={estilos.documentos}>
        <div style={estilos.docItem}>
          <span style={estilos.docLabel}>Carta:</span>
          <BadgeDocumento estado={carta?.estado || 'PENDIENTE'} />
        </div>
        <div style={estilos.docItem}>
          <span style={estilos.docLabel}>Convenio:</span>
          <BadgeDocumento estado={convenio?.estado || 'PENDIENTE'} />
        </div>
      </div>

      {/* ── Botón de acción ── */}
      <button
        type="button"
        onClick={irADetalle}
        style={estilos.btnGestionar}
        aria-label={`Gestionar vinculación de ${vinculacion.estudiante?.nombre}`}
      >
        Gestionar →
      </button>
    </div>
  );
}

// ── Estilos ───────────────────────────────────────────────────────────────────
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
  nombre:   { fontWeight: 700, fontSize: 14, color: '#111827' },
  subInfo:  { fontSize: 11, color: '#6b7280', marginTop: 2 },
  progreso: { fontSize: 13, fontWeight: 700 },
  fila:     { display: 'flex', gap: 8, fontSize: 13 },
  label:    { color: '#6b7280', minWidth: 65 },
  valor:    { color: '#111827', fontWeight: 500 },
  documentos: {
    display: 'flex',
    gap: 12,
    flexWrap: 'wrap',
    marginTop: 4,
  },
  docItem:  { display: 'flex', alignItems: 'center', gap: 6 },
  docLabel: { fontSize: 12, color: '#6b7280', fontWeight: 500 },
  btnGestionar: {
    marginTop: 4,
    padding: '8px 0',
    background: '#2563eb',
    color: '#fff',
    border: 'none',
    borderRadius: 6,
    fontSize: 13,
    fontWeight: 700,
    cursor: 'pointer',
  },
};
