import { useResumen } from '../hooks/useDashboard';
import TarjetaResumen from './TarjetaResumen';
import CentroAlertas from './CentroAlertas';
import useAuth from '../../../shared/hooks/useAuth';
import { PageHeader } from '@/shared/components/ui';

function tarjetasPorRol(rol, datos) {
  const d = datos || {};
  const mapa = {
    ADMIN: [
      { titulo: 'Usuarios activos', valor: d.usuariosActivos, icono: 'users', color: 'primary' },
      { titulo: 'Estudiantes en práctica', valor: d.estudiantesEnPractica, icono: 'graduation', color: 'emerald' },
      { titulo: 'Empresas vinculadas', valor: d.empresasActivas, icono: 'building', color: 'amber' },
      { titulo: 'Vacantes activas', valor: d.vacantesActivas, icono: 'clipboard', color: 'violet' },
    ],
    COORD_PRACTICA: [
      { titulo: 'Vacantes para aprobar', valor: d.vacantesParaAprobar, icono: 'clock', color: 'amber' },
      { titulo: 'Asignaciones activas', valor: d.asignacionesActivas, icono: 'link', color: 'primary' },
      { titulo: 'En práctica', valor: d.estudiantesEnPractica, icono: 'graduation', color: 'emerald' },
      { titulo: 'Cierres pendientes', valor: d.cierresPendientes, icono: 'clipboard', color: 'red' },
    ],
    COORD_ACADEMICA: [
      { titulo: 'Sin evaluar', valor: d.estudiantesSinEvaluar, icono: 'clipboard', color: 'amber' },
      { titulo: 'Aptos sin iniciar', valor: d.aptosSinIniciar, icono: 'award', color: 'emerald' },
    ],
    DOCENTE_ASESOR: [
      { titulo: 'Mis estudiantes', valor: d.estudiantesAsignados, icono: 'graduation', color: 'primary' },
      { titulo: 'Calificaciones pendientes', valor: d.calificacionesPendientes, icono: 'star', color: 'amber' },
    ],
    DIRECCION: [
      { titulo: 'Practicantes activos', valor: d.estudiantesEnPractica, icono: 'graduation', color: 'emerald' },
      { titulo: 'Empresas vinculadas', valor: d.empresasActivas, icono: 'building', color: 'primary' },
    ],
  };
  return mapa[rol] || mapa.ADMIN;
}

export default function DashboardPage() {
  const { usuario } = useAuth();
  const { data, isLoading, isError } = useResumen();

  const tarjetas = tarjetasPorRol(usuario?.rol, data);
  const maxValor = tarjetas.reduce((max, t) => Math.max(max, Number(t.valor) || 0), 0);

  return (
    <div>
      <PageHeader titulo="Panel de inicio" />

      {isLoading && <p className="text-sm text-gray-500">Cargando indicadores...</p>}
      {isError && <p className="text-sm text-red-600">No se pudo conectar con el servidor.</p>}

      {!isLoading && (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
          {tarjetas.map((t) => (
            <TarjetaResumen key={t.titulo} {...t} total={maxValor || undefined} />
          ))}
        </div>
      )}

      <div className="mt-6">
        <CentroAlertas />
      </div>
    </div>
  );
}
