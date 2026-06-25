const COLORES = {
  primary: 'bg-blue-500 dark:bg-primary-accent',
  emerald: 'bg-emerald-500 dark:bg-emerald-400',
  amber: 'bg-amber-500 dark:bg-amber-400',
  violet: 'bg-violet-500 dark:bg-violet-400',
  red: 'bg-red-500 dark:bg-red-400',
};

export default function BarraDistribucion({ items = [], titulo = 'Distribución' }) {
  const total = items.reduce((sum, item) => sum + (Number(item.valor) || 0), 0);

  if (total === 0) {
    return (
      <div className="ui-panel p-5">
        <h3 className="mb-2 text-sm font-semibold ui-text-body">{titulo}</h3>
        <p className="text-sm ui-text-muted">Sin datos para mostrar.</p>
      </div>
    );
  }

  return (
    <div className="ui-panel p-5">
      <h3 className="mb-4 text-sm font-semibold ui-text-body">{titulo}</h3>

      <div className="mb-4 flex h-3 overflow-hidden rounded-full bg-gray-100 dark:bg-dark-elevated">
        {items.map((item) => {
          const pct = ((Number(item.valor) || 0) / total) * 100;
          if (pct <= 0) return null;
          return (
            <div
              key={item.key ?? item.titulo}
              className={`${COLORES[item.color] ?? 'bg-gray-400'} transition-all duration-700`}
              style={{ width: `${pct}%` }}
              title={`${item.titulo}: ${item.valor}`}
            />
          );
        })}
      </div>

      <div className="space-y-2">
        {items.map((item) => {
          const valor = Number(item.valor) || 0;
          const pct = Math.round((valor / total) * 100);
          return (
            <div key={item.key ?? item.titulo} className="flex items-center gap-3 text-sm">
              <span
                className={`h-2.5 w-2.5 shrink-0 rounded-full ${COLORES[item.color] ?? 'bg-gray-400'}`}
              />
              <span className="flex-1 ui-text-body">{item.titulo}</span>
              <span className="font-semibold ui-text-title">{valor}</span>
              <span className="w-10 text-right text-xs ui-text-muted">{pct}%</span>
            </div>
          );
        })}
      </div>
    </div>
  );
}
