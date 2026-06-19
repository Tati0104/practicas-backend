import {
  ContenedorGrafica,
  GraficaArea,
  GraficaDonut,
  getPaleta,
  TarjetaKpi,
} from '@/shared/components/indicadores';
import useTheme from '@/shared/hooks/useTheme';

const tarjetasConfig = [
  { key: 'AL_DIA', titulo: 'Al día', icono: 'award' },
  { key: 'PENDIENTE', titulo: 'Pendiente', icono: 'clock' },
  { key: 'EN_ALERTA', titulo: 'En alerta', icono: 'clipboard' },
];

export default function IndicadoresSeguimiento({ practicas = [] }) {
  const { esOscuro } = useTheme();
  const paleta = getPaleta(esOscuro);
  const conteos = {
    AL_DIA: practicas.filter((p) => p.estadoSeguimiento === 'AL_DIA').length,
    PENDIENTE: practicas.filter((p) => p.estadoSeguimiento === 'PENDIENTE').length,
    EN_ALERTA: practicas.filter((p) => p.estadoSeguimiento === 'EN_ALERTA').length,
  };

  const datosArea = [
    { nombre: 'Al día', total: conteos.AL_DIA, alerta: conteos.EN_ALERTA },
    { nombre: 'Pendiente', total: conteos.PENDIENTE, alerta: 0 },
    { nombre: 'En alerta', total: conteos.EN_ALERTA, alerta: conteos.EN_ALERTA },
  ];

  const datosDonut = [
    { nombre: 'Al día', valor: conteos.AL_DIA, color: paleta.emerald },
    { nombre: 'Pendiente', valor: conteos.PENDIENTE, color: paleta.accent },
    { nombre: 'En alerta', valor: conteos.EN_ALERTA, color: paleta.red },
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

      {practicas.length > 0 && (
        <div className="grid grid-cols-1 gap-3 lg:grid-cols-3">
          <ContenedorGrafica className="lg:col-span-2" titulo="Estado del seguimiento">
            <GraficaArea
              datos={datosArea}
              series={[
                { key: 'total', nombre: 'Prácticas', color: paleta.primary },
                { key: 'alerta', nombre: 'En alerta', color: paleta.red },
              ]}
              altura={200}
            />
          </ContenedorGrafica>
          <ContenedorGrafica titulo="Distribución">
            <GraficaDonut
              datos={datosDonut}
              etiquetaCentral="Al día"
              valorCentral={conteos.AL_DIA}
              altura={180}
            />
          </ContenedorGrafica>
        </div>
      )}
    </div>
  );
}
