import {
  Bar,
  BarChart,
  CartesianGrid,
  Legend,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import { PALETA } from './paletaIndicadores';

const tooltipStyle = {
  borderRadius: '10px',
  border: '1px solid #E2E8F0',
  fontSize: '12px',
};

export default function GraficaBarras({
  datos = [],
  series = [],
  altura = 280,
  apilada = false,
}) {
  if (!datos.length) {
    return <p className="py-12 text-center text-sm text-gray-400">Sin datos para graficar</p>;
  }

  return (
    <ResponsiveContainer width="100%" height={altura}>
      <BarChart data={datos} margin={{ top: 8, right: 8, left: -16, bottom: 0 }}>
        <CartesianGrid strokeDasharray="3 3" stroke={PALETA.grid} vertical={false} />
        <XAxis
          dataKey="nombre"
          tick={{ fill: PALETA.textMuted, fontSize: 11 }}
          axisLine={false}
          tickLine={false}
        />
        <YAxis
          tick={{ fill: PALETA.textMuted, fontSize: 11 }}
          axisLine={false}
          tickLine={false}
          allowDecimals={false}
        />
        <Tooltip contentStyle={tooltipStyle} />
        {series.length > 1 && (
          <Legend
            wrapperStyle={{ fontSize: '12px', paddingTop: '8px' }}
            iconType="circle"
            iconSize={8}
          />
        )}
        {series.map((s) => (
          <Bar
            key={s.key}
            dataKey={s.key}
            name={s.nombre}
            fill={s.color}
            radius={[6, 6, 0, 0]}
            stackId={apilada ? 'stack' : undefined}
            maxBarSize={apilada ? undefined : 48}
          />
        ))}
      </BarChart>
    </ResponsiveContainer>
  );
}
