import {
  Area,
  AreaChart,
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

export default function GraficaArea({ datos = [], series = [], altura = 260 }) {
  if (!datos.length) {
    return <p className="py-12 text-center text-sm text-gray-400">Sin datos para graficar</p>;
  }

  return (
    <ResponsiveContainer width="100%" height={altura}>
      <AreaChart data={datos} margin={{ top: 8, right: 8, left: -16, bottom: 0 }}>
        <defs>
          {series.map((s) => (
            <linearGradient key={s.key} id={`grad-${s.key}`} x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor={s.color} stopOpacity={0.35} />
              <stop offset="100%" stopColor={s.color} stopOpacity={0.02} />
            </linearGradient>
          ))}
        </defs>
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
          <Area
            key={s.key}
            type="monotone"
            dataKey={s.key}
            name={s.nombre}
            stroke={s.color}
            strokeWidth={2}
            fill={`url(#grad-${s.key})`}
          />
        ))}
      </AreaChart>
    </ResponsiveContainer>
  );
}
