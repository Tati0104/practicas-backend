import TarjetaResumen from '@/modules/dashboard/components/TarjetaResumen';
import { LoadingState, PageHeader } from '@/shared/components/ui';
import { useReporteResumen } from '../hooks/useReportes';

const INDICADORES = [
  { key: 'totalVacantes', titulo: 'Total vacantes', icono: 'clipboard', color: 'violet' },
  { key: 'vacantesActivas', titulo: 'Vacantes activas', icono: 'clipboard', color: 'emerald' },
  { key: 'vacantesPendientes', titulo: 'Vacantes pendientes', icono: 'clock', color: 'amber' },
  { key: 'totalAsignaciones', titulo: 'Total asignaciones', icono: 'link', color: 'primary' },
  { key: 'asignacionesVinculadas', titulo: 'Asignaciones vinculadas', icono: 'link', color: 'emerald' },
  { key: 'asignacionesCanceladas', titulo: 'Asignaciones canceladas', icono: 'link', color: 'red' },
  { key: 'estudiantesRegistrados', titulo: 'Estudiantes registrados', icono: 'users', color: 'primary' },
];

export default function ReportesPage() {
  const { data, isLoading, isError } = useReporteResumen();

  return (
    <div>
      <PageHeader
        titulo="Reportes"
        descripcion="Indicadores consolidados del sistema de prácticas profesionales."
      />

      {isLoading && <LoadingState mensaje="Cargando indicadores..." />}

      {isError && (
        <p className="text-sm text-red-600">No se pudieron cargar los reportes.</p>
      )}

      {!isLoading && !isError && (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
          {INDICADORES.map(({ key, titulo, icono, color }) => (
            <TarjetaResumen
              key={key}
              titulo={titulo}
              valor={data?.[key]}
              icono={icono}
              color={color}
            />
          ))}
        </div>
      )}
    </div>
  );
}
