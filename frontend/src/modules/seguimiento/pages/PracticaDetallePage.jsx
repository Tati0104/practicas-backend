// src/modules/seguimiento/pages/PracticaDetallePage.jsx

/**
 * Página de detalle de una práctica en el módulo de Seguimiento.
 * Muestra info general, timeline de eventos y panel de alertas.
 * Según el rol, muestra el botón de acción correspondiente:
 *   - COORD_PRACTICA / DOCENTE_ASESOR → Registrar observación
 *   - TUTOR_EMPRESARIAL               → Registrar avance
 *   - ESTUDIANTE                      → Nueva bitácora
 */
import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { usePracticaSeguimiento }    from '../hooks/usePracticaSeguimiento';
import { useSeguimientoMutaciones }  from '../hooks/useSeguimientoMutaciones';
import { usePermisos }               from '../../../shared/hooks/usePermisos';
import TimelineSeguimiento           from '../components/TimelineSeguimiento';
import AlertasPanel                  from '../components/AlertasPanel';
import ObservacionModal              from '../components/ObservacionModal';
import AvanceTutorModal              from '../components/AvanceTutorModal';
import BitacoraModal                 from '../components/BitacoraModal';

const BADGE = {
  AL_DIA:    { label: 'Al día',    bg: '#dcfce7', color: '#15803d' },
  PENDIENTE: { label: 'Pendiente', bg: '#fef3c7', color: '#b45309' },
  EN_ALERTA: { label: 'En alerta', bg: '#fee2e2', color: '#b91c1c' },
};

export default function PracticaDetallePage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { practica, timeline, isLoading, isError } = usePracticaSeguimiento(id);
  const { usuario } = usePermisos();
  const rol = usuario?.rol;

  const [modalObservacion, setModalObservacion] = useState(false);
  const [modalAvance,      setModalAvance]      = useState(false);
  const [modalBitacora,    setModalBitacora]    = useState(false);

  const { registrarObservacion, registrarAvance, registrarBitacora } =
    useSeguimientoMutaciones({ onSuccess: () => { setModalObservacion(false); setModalAvance(false); setModalBitacora(false); } });

  if (isLoading) return <div style={{ padding: 40, textAlign: 'center', color: '#6b7280' }}>Cargando detalle...</div>;
  if (isError || !practica) return <div style={{ padding: 20, color: '#b91c1c' }}>Error al cargar la práctica.</div>;

  const badge = BADGE[practica.estado] || { label: practica.estado, bg: '#f3f4f6', color: '#374151' };

  return (
    <div style={{ padding: 20, fontFamily: 'Arial, sans-serif', maxWidth: 1100, margin: '0 auto' }}>
      {/* Encabezado */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
        <div>
          <button onClick={() => navigate('/seguimiento')} style={{ background: 'none', border: 'none', color: '#2563eb', cursor: 'pointer', fontSize: 14, padding: 0, marginBottom: 8 }}>
            ← Volver al tablero
          </button>
          <h1 style={{ margin: 0, fontSize: 20, fontWeight: 800, color: '#111827' }}>
            {practica.estudiante?.nombre}
          </h1>
          <div style={{ fontSize: 13, color: '#6b7280' }}>{practica.estudiante?.codigo} — {practica.estudiante?.programa}</div>
        </div>
        <span style={{ background: badge.bg, color: badge.color, borderRadius: 20, padding: '4px 14px', fontSize: 13, fontWeight: 700 }}>
          {badge.label}
        </span>
      </div>

      <div style={{ display: 'flex', gap: 20, alignItems: 'flex-start' }}>
        {/* Columna principal */}
        <div style={{ flex: 1, minWidth: 0 }}>
          {/* Info general */}
          <div style={{ background: '#fff', border: '1px solid #e5e7eb', borderRadius: 10, padding: '16px 20px', marginBottom: 20 }}>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 12 }}>
              {[
                { label: 'Empresa',   valor: practica.empresa },
                { label: 'Cargo',     valor: practica.cargo },
                { label: 'Docente',   valor: practica.docente },
                { label: 'Tutor',     valor: practica.tutor },
                { label: 'Inicio',    valor: practica.fechaInicio },
                { label: 'Fin',       valor: practica.fechaFin },
              ].map(({ label, valor }) => (
                <div key={label}>
                  <div style={{ fontSize: 11, color: '#9ca3af', fontWeight: 600, textTransform: 'uppercase' }}>{label}</div>
                  <div style={{ fontSize: 14, color: '#111827', fontWeight: 500 }}>{valor || '—'}</div>
                </div>
              ))}
            </div>
            {/* Barra de avance */}
            <div style={{ marginTop: 16 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13, color: '#374151', marginBottom: 4 }}>
                <span>Avance general</span><span>{practica.porcentajeAvance || 0}%</span>
              </div>
              <div style={{ background: '#e5e7eb', borderRadius: 99, height: 8 }}>
                <div style={{ background: '#2563eb', width: `${practica.porcentajeAvance || 0}%`, height: '100%', borderRadius: 99 }} />
              </div>
            </div>
          </div>

          {/* Botón de acción según rol */}
          <div style={{ marginBottom: 20 }}>
            {(rol === 'COORD_PRACTICA' || rol === 'DOCENTE_ASESOR') && (
              <button onClick={() => setModalObservacion(true)} style={btnObservacion}>+ Registrar observación</button>
            )}
            {rol === 'TUTOR_EMPRESARIAL' && (
              <button onClick={() => setModalAvance(true)} style={btnAvance}>+ Registrar avance</button>
            )}
            {rol === 'ESTUDIANTE' && (
              <button onClick={() => setModalBitacora(true)} style={btnBitacora}>+ Nueva bitácora</button>
            )}
          </div>

          {/* Timeline */}
          <div style={{ background: '#fff', border: '1px solid #e5e7eb', borderRadius: 10, padding: '16px 20px' }}>
            <h2 style={{ margin: '0 0 16px', fontSize: 16, fontWeight: 700, color: '#111827' }}>Historial de actividades</h2>
            <TimelineSeguimiento timeline={timeline} />
          </div>
        </div>

        {/* Panel alertas */}
        <div style={{ width: 280, flexShrink: 0 }}>
          <AlertasPanel practicaId={Number(id)} />
        </div>
      </div>

      {/* Modales */}
      <ObservacionModal isOpen={modalObservacion} practicaId={Number(id)} onClose={() => setModalObservacion(false)} onGuardar={(dto) => registrarObservacion.mutate(dto)} isPending={registrarObservacion.isPending} />
      <AvanceTutorModal isOpen={modalAvance} practicaId={Number(id)} onClose={() => setModalAvance(false)} onGuardar={(dto) => registrarAvance.mutate(dto)} isPending={registrarAvance.isPending} />
      <BitacoraModal isOpen={modalBitacora} practicaId={Number(id)} onClose={() => setModalBitacora(false)} onGuardar={(dto) => registrarBitacora.mutate(dto)} isPending={registrarBitacora.isPending} />
    </div>
  );
}

const btnObservacion = { padding: '9px 18px', background: '#2563eb', color: '#fff', border: 'none', borderRadius: 8, fontSize: 14, fontWeight: 700, cursor: 'pointer' };
const btnAvance      = { padding: '9px 18px', background: '#15803d', color: '#fff', border: 'none', borderRadius: 8, fontSize: 14, fontWeight: 700, cursor: 'pointer' };
const btnBitacora    = { padding: '9px 18px', background: '#7c3aed', color: '#fff', border: 'none', borderRadius: 8, fontSize: 14, fontWeight: 700, cursor: 'pointer' };
