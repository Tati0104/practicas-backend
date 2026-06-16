import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts';
import { PALETA } from './paletaIndicadores';

const tooltipStyle = {
  borderRadius: '10px',
  border: '1px solid #E2E8F0',
  fontSize: '12px',
};

export default function GraficaDonut({
  datos = [],
  altura = 220,
  etiquetaCentral,
  valorCentral,
}) {
  const total = datos.reduce((sum, d) => sum + (Number(d.valor) || 0), 0);
  const filtrados = datos.filter((d) => Number(d.valor) > 0);

  if (total === 0) {
    return <p className="py-12 text-center text-sm text-gray-400">Sin datos para graficar</p>;
  }

  const pctCentral =
    valorCentral != null && total > 0
      ? Math.round((Number(valorCentral) / total) * 100)
      : null;

  return (
    <div>
      <div className="relative">
        <ResponsiveContainer width="100%" height={altura}>
          <PieChart>
            <Pie
              data={filtrados}
              dataKey="valor"
              nameKey="nombre"
              cx="50%"
              cy="50%"
              innerRadius="58%"
              outerRadius="82%"
              paddingAngle={3}
              strokeWidth={0}
            >
              {filtrados.map((entry) => (
                <Cell key={entry.nombre} fill={entry.color} />
              ))}
            </Pie>
            <Tooltip contentStyle={tooltipStyle} />
          </PieChart>
        </ResponsiveContainer>
        {(pctCentral != null || etiquetaCentral) && (
          <div className="pointer-events-none absolute inset-0 flex flex-col items-center justify-center">
            {pctCentral != null && (
              <span className="text-2xl font-bold text-primary">{pctCentral}%</span>
            )}
            {etiquetaCentral && (
              <span className="text-[11px] text-gray-500">{etiquetaCentral}</span>
            )}
          </div>
        )}
      </div>

      <ul className="mt-2 space-y-2">
        {filtrados.map((item) => {
          const pct = Math.round(((Number(item.valor) || 0) / total) * 100);
          return (
            <li key={item.nombre} className="flex items-center gap-2 text-sm">
              <span
                className="h-2.5 w-2.5 shrink-0 rounded-full"
                style={{ backgroundColor: item.color }}
              />
              <span className="flex-1 text-gray-600">{item.nombre}</span>
              <span className="font-semibold tabular-nums text-gray-900">{item.valor}</span>
              <span className="w-9 text-right text-xs text-gray-400">{pct}%</span>
            </li>
          );
        })}
      </ul>
    </div>
  );
}
