// src/modules/asignaciones/pages/AsignacionDetallePage.jsx

/**
 * Página de detalle de una asignación.
 *
 * Muestra:
 *   - Datos del estudiante
 *   - Datos de la vacante y empresa
 *   - Programa académico
 *   - Fecha de asignación
 *   - Estado actual (con Badge)
 *   - Historial de estados (timeline)
 *
 * Usa useAsignacionDetalle que llama a GET /api/asignaciones/{id}
 */
import { useParams, useNavigate } from 'react-router-dom';
import { useAsignacionDetalle } from '../hooks/useAsignacionDetalle';
import BadgeAsignacion from '../components/BadgeAsignacion';
import HistorialEstados from '../components/HistorialEstados';

export default function AsignacionDetallePage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { asignacion, isLoading, isError } = useAsignacionDetalle(id);

  if (isLoading) {
    return (
      <div style={estilos.centrado}>
        <p style={{ color: '#6b7280' }}>Cargando detalle de la asignación...</p>
      </div>
    );
  }

  if (isError || !asignacion) {
    return (
      <div style={estilos.centrado}>
        <div style={estilos.errorBox}>
          <p>No se pudo cargar la asignación. Verifica el ID o intenta más tarde.</p>
          <button onClick={() => navigate('/asignaciones')} style={estilos.btnVolver}>
            ← Volver al listado
          </button>
        </div>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: 720, margin: '0 auto', padding: 24, fontFamily: 'Arial, sans-serif' }}>
      {/* Botón volver */}
      <button onClick={() => navigate('/asignaciones')} style={estilos.btnVolver}>
        ← Volver
      </button>

      {/* Título + Estado */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', margin: '16px 0' }}>
        <h1 style={{ fontSize: 22, fontWeight: 800, color: '#111827', margin: 0 }}>
          Detalle de Asignación
        </h1>
        <BadgeAsignacion estado={asignacion.estado} />
      </div>

      {/* Sección: Estudiante */}
      <Seccion titulo="👤 Estudiante">
        <Fila label="Nombre"    valor={asignacion.estudiante?.nombre} />
        <Fila label="Código"    valor={asignacion.estudiante?.codigo} />
        <Fila label="Programa"  valor={asignacion.estudiante?.programa} />
      </Seccion>

      {/* Sección: Vacante / Empresa */}
      <Seccion titulo="🏢 Vacante y Empresa">
        <Fila label="Cargo"    valor={asignacion.vacante?.cargo} />
        <Fila label="Empresa"  valor={asignacion.vacante?.empresa} />
        <Fila label="Modalidad" valor={asignacion.vacante?.modalidad} />
      </Seccion>

      {/* Sección: Asignación */}
      <Seccion titulo="📋 Datos de la asignación">
        <Fila
          label="Fecha"
          valor={
            asignacion.fechaAsignacion
              ? new Date(asignacion.fechaAsignacion).toLocaleDateString('es-CO', {
                  day: '2-digit', month: 'long', year: 'numeric',
                })
              : '—'
          }
        />
        <Fila label="Estado" valor={<BadgeAsignacion estado={asignacion.estado} />} />
      </Seccion>

      {/* Sección: Historial */}
      <Seccion titulo="🕓 Historial de estados">
        <HistorialEstados historial={asignacion.historial || []} />
      </Seccion>
    </div>
  );
}

// Componentes auxiliares de layout
function Seccion({ titulo, children }) {
  return (
    <div style={estilos.seccion}>
      <h2 style={estilos.seccionTitulo}>{titulo}</h2>
      <div style={estilos.seccionCuerpo}>{children}</div>
    </div>
  );
}

function Fila({ label, valor }) {
  return (
    <div style={estilos.fila}>
      <span style={estilos.label}>{label}</span>
      <span style={estilos.valor}>{valor || '—'}</span>
    </div>
  );
}

const estilos = {
  centrado: { display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 200 },
  errorBox: {
    background: '#fee2e2', border: '1px solid #fca5a5',
    borderRadius: 8, padding: 20, textAlign: 'center', color: '#991b1b',
  },
  btnVolver: {
    padding: '6px 14px',
    background: '#f1f5f9',
    border: '1px solid #d1d5db',
    borderRadius: 6,
    fontSize: 13,
    cursor: 'pointer',
    color: '#374151',
  },
  seccion: {
    background: '#fff',
    border: '1px solid #e5e7eb',
    borderRadius: 10,
    marginBottom: 16,
    overflow: 'hidden',
  },
  seccionTitulo: {
    fontSize: 14,
    fontWeight: 700,
    color: '#374151',
    background: '#f8fafc',
    padding: '10px 16px',
    margin: 0,
    borderBottom: '1px solid #e5e7eb',
  },
  seccionCuerpo: { padding: '10px 16px' },
  fila:  { display: 'flex', gap: 12, padding: '6px 0', fontSize: 13, borderBottom: '1px solid #f1f5f9' },
  label: { color: '#6b7280', minWidth: 100, fontWeight: 500 },
  valor: { color: '#111827' },
};
