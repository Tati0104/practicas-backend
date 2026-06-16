const COLORES = {
  primary: '#2563eb',
  emerald: '#059669',
  amber: '#d97706',
  violet: '#7c3aed',
  red: '#dc2626',
};

export default function AnilloProgreso({
  valor = 0,
  total,
  size = 76,
  grosor = 7,
  color = 'primary',
  mostrarPorcentaje = true,
}) {
  const colorStroke = COLORES[color] ?? color;
  const baseTotal = total > 0 ? total : Math.max(valor, 1);
  const porcentaje = Math.min(100, Math.round((valor / baseTotal) * 100));
  const radio = (size - grosor) / 2;
  const circunferencia = 2 * Math.PI * radio;
  const offset = circunferencia - (porcentaje / 100) * circunferencia;

  return (
    <div className="relative shrink-0" style={{ width: size, height: size }}>
      <svg width={size} height={size} className="-rotate-90">
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radio}
          fill="none"
          stroke="#e5e7eb"
          strokeWidth={grosor}
        />
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radio}
          fill="none"
          stroke={colorStroke}
          strokeWidth={grosor}
          strokeLinecap="round"
          strokeDasharray={circunferencia}
          strokeDashoffset={offset}
          className="transition-all duration-700 ease-out"
        />
      </svg>
      {mostrarPorcentaje && (
        <span className="absolute inset-0 flex items-center justify-center text-xs font-bold text-gray-700">
          {porcentaje}%
        </span>
      )}
    </div>
  );
}
