import { useResumen } from '../hooks/useDashboard';
import TarjetaResumen from './TarjetaResumen';
import CentroAlertas from './CentroAlertas';
import useAuth from '../../../shared/hooks/useAuth';
import {
  ContenedorGrafica,
  GraficaBarras,
  GraficaDonut,
  PALETA,
  TarjetaKpi,
} from '@/shared/components/indicadores';
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
    TUTOR_EMPRESARIAL: [
      { titulo: 'Mis practicantes', valor: d.estudiantesAsignados, icono: 'graduation', color: 'primary' },
      { titulo: 'Firmas pendientes', valor: d.firmasPendientes, icono: 'clipboard', color: 'amber' },
    ],
    DIRECCION: [
      { titulo: 'Practicantes activos', valor: d.estudiantesEnPractica, icono: 'graduation', color: 'emerald' },
      { titulo: 'Empresas vinculadas', valor: d.empresasActivas, icono: 'building', color: 'primary' },
    ],
  };
  return mapa[rol] || mapa.COORD_PRACTICA;
}

function iconoKpi(icono) {
  const mapa = {
    users: 'users',
    graduation: 'graduation',
    building: 'building',
    clipboard: 'clipboard',
    clock: 'clock',
    link: 'link',
    star: 'star',
    award: 'award',
  };
  return mapa[icono] ?? 'clipboard';
}

export default function DashboardPage() {
  const { usuario } = useAuth();
  const { data, isLoading, isError } = useResumen();

  const tarjetas = tarjetasPorRol(usuario?.rol, data);
  const maxValor = tarjetas.reduce((max, t) => Math.max(max, Number(t.valor) || 0), 0);

  const datosGrafica = tarjetas.map((t) => ({
    nombre: t.titulo.length > 14 ? `${t.titulo.slice(0, 12)}…` : t.titulo,
    valor: Number(t.valor) || 0,
  }));

  const datosDonut = tarjetas.map((t, i) => ({
    nombre: t.titulo,
    valor: Number(t.valor) || 0,
    color: [PALETA.primary, PALETA.emerald, PALETA.accent, PALETA.violet, PALETA.red][i % 5],
  }));

  const primera = tarjetas[0];

  return (
    <div className="space-y-6">
      <PageHeader titulo="Panel de inicio" />

      {isLoading && <p className="text-sm text-gray-500">Cargando indicadores...</p>}
      {isError && <p className="text-sm text-red-600">No se pudo conectar con el servidor.</p>}

      {!isLoading && tarjetas.length > 0 && (
        <>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
            {primera && (
              <TarjetaKpi
                titulo={primera.titulo}
                valor={primera.valor}
                icono={iconoKpi(primera.icono)}
                destacada
              />
            )}
            {tarjetas.slice(1).map((t) => (
              <TarjetaKpi
                key={t.titulo}
                titulo={t.titulo}
                valor={t.valor}
                icono={iconoKpi(t.icono)}
              />
            ))}
          </div>

          {tarjetas.length >= 2 && (
            <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
              <ContenedorGrafica
                className="lg:col-span-2"
                titulo="Resumen por indicador"
                descripcion="Comparativa visual de tus métricas principales"
              >
                <GraficaBarras
                  datos={datosGrafica}
                  series={[{ key: 'valor', nombre: 'Cantidad', color: PALETA.primary }]}
                  altura={240}
                />
              </ContenedorGrafica>

              <ContenedorGrafica titulo="Distribución" descripcion="Proporción entre indicadores">
                <GraficaDonut datos={datosDonut.filter((d) => d.valor > 0)} />
              </ContenedorGrafica>
            </div>
          )}

          {tarjetas.length === 1 && (
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
              <TarjetaResumen {...tarjetas[0]} total={maxValor || undefined} />
            </div>
          )}
        </>
      )}

      <CentroAlertas />
    </div>
  );
}
