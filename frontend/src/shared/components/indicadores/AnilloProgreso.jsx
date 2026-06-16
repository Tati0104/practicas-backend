import useTheme from '@/shared/hooks/useTheme';

const COLORES = {
  primary: '#19426B',
  emerald: '#059669',
  amber: '#D97706',
  violet: '#7C3AED',
  red: '#DC2626',
};

const COLORES_OSCURO = {
  primary: '#2E6DA8',
  emerald: '#10B981',
  amber: '#F59E0B',
  violet: '#A78BFA',
  red: '#F87171',
};

export default function AnilloProgreso({
  valor = 0,
  total,
  size = 76,
  grosor = 7,
  color = 'primary',
  mostrarPorcentaje = true,
}) {
  const { esOscuro } = useTheme();
  const mapa = esOscuro ? COLORES_OSCURO : COLORES;
  const colorStroke = mapa[color] ?? color;
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
          className="stroke-gray-200 dark:stroke-dark-border"
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
        <span className="absolute inset-0 flex items-center justify-center text-xs font-bold text-gray-700 dark:text-slate-200">
          {porcentaje}%
        </span>
      )}
    </div>
  );
}
