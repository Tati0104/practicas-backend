import { useResumen } from '../hooks/useDashboard';
import TarjetaResumen from './TarjetaResumen';
import CentroAlertas  from './CentroAlertas';
import useAuth        from '../../../shared/hooks/useAuth';

// Tarjetas según el rol del usuario
function tarjetasPorRol(rol, datos) {
  const d = datos || {};
  const mapa = {
    ADMIN: [
      { titulo: 'Usuarios activos',       valor: d.usuariosActivos,     icono: '👥', color: '#1e3a5f' },
      { titulo: 'Estudiantes en práctica', valor: d.estudiantesEnPractica, icono: '🎓', color: '#059669' },
      { titulo: 'Empresas vinculadas',    valor: d.empresasActivas,     icono: '🏢', color: '#d97706' },
      { titulo: 'Vacantes activas',       valor: d.vacantesActivas,     icono: '📋', color: '#7c3aed' },
    ],
    COORD_PRACTICA: [
      { titulo: 'Vacantes para aprobar',  valor: d.vacantesParaAprobar, icono: '⏳', color: '#d97706' },
      { titulo: 'Asignaciones activas',   valor: d.asignacionesActivas, icono: '🔗', color: '#1e3a5f' },
      { titulo: 'En práctica',            valor: d.estudiantesEnPractica, icono: '🎓', color: '#059669' },
      { titulo: 'Cierres pendientes',     valor: d.cierresPendientes,   icono: '📝', color: '#dc2626' },
    ],
    COORD_ACADEMICA: [
      { titulo: 'Sin evaluar',            valor: d.estudiantesSinEvaluar, icono: '📋', color: '#d97706' },
      { titulo: 'Aptos sin iniciar',      valor: d.aptosSinIniciar,     icono: '✅', color: '#059669' },
    ],
    DOCENTE_ASESOR: [
      { titulo: 'Mis estudiantes',        valor: d.estudiantesAsignados, icono: '🎓', color: '#1e3a5f' },
      { titulo: 'Calificaciones pendientes', valor: d.calificacionesPendientes, icono: '⭐', color: '#d97706' },
    ],
    DIRECCION: [
      { titulo: 'Practicantes activos',   valor: d.estudiantesEnPractica, icono: '🎓', color: '#059669' },
      { titulo: 'Empresas vinculadas',    valor: d.empresasActivas,     icono: '🏢', color: '#1e3a5f' },
    ],
  };
  return mapa[rol] || mapa['ADMIN'];
}

export default function DashboardPage() {
  const { usuario }                    = useAuth();
  const { data, isLoading, isError }   = useResumen();

  return (
    <div style={estilos.pagina}>
      <h2 style={estilos.titulo}>Panel de inicio</h2>

      {isLoading && <p style={estilos.msg}>Cargando indicadores...</p>}
      {isError   && <p style={estilos.error}>No se pudo conectar con el servidor.</p>}

      {/* Tarjetas de resumen */}
      {!isLoading && (
        <div style={estilos.grid}>
          {tarjetasPorRol(usuario?.rol, data).map((t, i) => (
            <TarjetaResumen key={i} {...t} />
          ))}
        </div>
      )}

      {/* Centro de alertas */}
      <div style={{ marginTop: 24 }}>
        <CentroAlertas />
      </div>
    </div>
  );
}

const estilos = {
  pagina: { fontFamily: 'Arial, sans-serif' },
  titulo: { fontSize: 20, fontWeight: 700, color: '#1e3a5f', margin: '0 0 20px' },
  grid:   {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))',
    gap: 16
  },
  msg:   { color: '#6b7280', fontSize: 14 },
  error: { color: '#dc2626', fontSize: 14 }
};