import {
  ContenedorGrafica,
  GraficaArea,
  GraficaBarras,
  GraficaDonut,
  TarjetaKpi,
  datosAreaEvolucion,
  datosBarrasComparativa,
  datosDonutAsignaciones,
  datosDonutVacantes,
  normalizarReporte,
  seriesAreaComparativa,
  seriesBarrasVacantesAsignaciones,
} from '@/shared/components/indicadores';
import { LoadingState, PageHeader } from '@/shared/components/ui';
import { useReporteResumen } from '../hooks/useReportes';

export default function ReportesPage() {
  const { data: raw, isLoading, isError } = useReporteResumen();
  const data = normalizarReporte(raw);

  const tasaVinculacion =
    data && data.totalAsignaciones > 0
      ? Math.round((data.asignacionesVinculadas / data.totalAsignaciones) * 100)
      : 0;

  return (
    <div className="space-y-6">
      <PageHeader
        titulo="Indicadores"
        descripcion="Panorama visual del sistema de prácticas empresariales."
      />

      {isLoading && <LoadingState mensaje="Cargando indicadores..." />}

      {isError && (
        <p className="text-sm text-red-600">No se pudieron cargar los reportes.</p>
      )}

      {!isLoading && !isError && data && (
        <>
          {/* Fila KPI — estilo mockup con tarjeta destacada */}
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
            <TarjetaKpi
              titulo="Estudiantes registrados"
              valor={data.estudiantesRegistrados}
              subtitulo="Base académica del sistema"
              icono="users"
              destacada
            />
            <TarjetaKpi
              titulo="Vacantes activas"
              valor={data.vacantesActivas}
              subtitulo={`de ${data.totalVacantes} totales`}
              icono="clipboard"
            />
            <TarjetaKpi
              titulo="Asignaciones vinculadas"
              valor={data.asignacionesVinculadas}
              subtitulo={`${tasaVinculacion}% del total`}
              icono="link"
            />
            <TarjetaKpi
              titulo="Vacantes pendientes"
              valor={data.vacantesPendientes}
              subtitulo="Por aprobar o publicar"
              icono="clock"
            />
          </div>

          {/* Barras + Donut */}
          <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
            <ContenedorGrafica
              className="lg:col-span-2"
              titulo="Comparativa vacantes y asignaciones"
              descripcion="Distribución por estado en cada módulo"
            >
              <GraficaBarras
                datos={datosBarrasComparativa(data)}
                series={seriesBarrasVacantesAsignaciones()}
              />
            </ContenedorGrafica>

            <ContenedorGrafica
              titulo="Estado de asignaciones"
              descripcion="Proporción vinculadas vs en proceso vs canceladas"
            >
              <GraficaDonut
                datos={datosDonutAsignaciones(data)}
                etiquetaCentral="Vinculadas"
                valorCentral={data.asignacionesVinculadas}
              />
            </ContenedorGrafica>
          </div>

          {/* Área + Donut vacantes */}
          <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
            <ContenedorGrafica
              className="lg:col-span-2"
              titulo="Panorama general"
              descripcion="Totales vs activos en vacantes, asignaciones y estudiantes"
            >
              <GraficaArea
                datos={datosAreaEvolucion(data)}
                series={seriesAreaComparativa()}
              />
            </ContenedorGrafica>

            <ContenedorGrafica titulo="Estado de vacantes" descripcion="Activas, pendientes y otras">
              <GraficaDonut
                datos={datosDonutVacantes(data)}
                etiquetaCentral="Activas"
                valorCentral={data.vacantesActivas}
              />
            </ContenedorGrafica>
          </div>
        </>
      )}
    </div>
  );
}
