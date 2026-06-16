import {
  ContenedorGrafica,
  GraficaBarras,
  GraficaDonut,
  PALETA,
  TarjetaKpi,
} from '@/shared/components/indicadores';

const tarjetasConfig = [
  { key: 'activas', titulo: 'Asignaciones activas', icono: 'clipboard' },
  { key: 'vinculacion', titulo: 'En vinculación', icono: 'link' },
  { key: 'canceladas', titulo: 'Canceladas este mes', icono: 'clock' },
];

export default function IndicadoresAsignaciones({ asignaciones = [] }) {
  const ahora = new Date();

  const conteos = {
    activas: asignaciones.filter(
      (a) => a.estado === 'ASIGNADA' || a.estado === 'EN_PROCESO_VINCULACION'
    ).length,
    vinculacion: asignaciones.filter((a) => a.estado === 'EN_PROCESO_VINCULACION').length,
    canceladas: asignaciones.filter((a) => {
      if (a.estado !== 'CANCELADA' || !a.fechaAsignacion) return false;
      const fecha = new Date(a.fechaAsignacion);
      return fecha.getMonth() === ahora.getMonth() && fecha.getFullYear() === ahora.getFullYear();
    }).length,
  };

  const datosBarras = tarjetasConfig.map(({ key, titulo }) => ({
    nombre: titulo.split(' ').slice(-1)[0],
    cantidad: conteos[key],
  }));

  const datosDonut = [
    { nombre: 'Activas', valor: conteos.activas, color: PALETA.primary },
    { nombre: 'En vinculación', valor: conteos.vinculacion, color: PALETA.accent },
    { nombre: 'Canceladas', valor: conteos.canceladas, color: PALETA.red },
  ].filter((d) => d.valor > 0);

  return (
    <div className="mb-5 space-y-4">
      <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
        {tarjetasConfig.map(({ key, titulo, icono }, i) => (
          <TarjetaKpi
            key={key}
            titulo={titulo}
            valor={conteos[key]}
            icono={icono}
            destacada={i === 0}
          />
        ))}
      </div>

      {asignaciones.length > 0 && (
        <div className="grid grid-cols-1 gap-3 lg:grid-cols-3">
          <ContenedorGrafica className="lg:col-span-2" titulo="Distribución de asignaciones">
            <GraficaBarras
              datos={datosBarras}
              series={[{ key: 'cantidad', nombre: 'Cantidad', color: PALETA.primary }]}
              altura={200}
            />
          </ContenedorGrafica>
          <ContenedorGrafica titulo="Proporción">
            <GraficaDonut datos={datosDonut} altura={180} />
          </ContenedorGrafica>
        </div>
      )}
    </div>
  );
}
