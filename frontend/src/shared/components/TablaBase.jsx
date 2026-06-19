export default function TablaBase({ columnas, datos, cargando, sinDatos = 'No hay registros' }) {
  if (cargando) {
    return <p className="text-sm ui-text-muted">Cargando...</p>;
  }

  return (
    <div className="overflow-x-auto rounded-2xl ui-border border">
      <table className="w-full border-collapse text-sm">
        <thead>
          <tr className="border-b ui-border bg-slate-50 dark:bg-dark-elevated">
            {columnas.map((col) => (
              <th
                key={col.key}
                className="px-3.5 py-2.5 text-left text-xs font-semibold uppercase tracking-wide ui-text-muted"
              >
                {col.titulo}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {datos.length === 0 ? (
            <tr>
              <td colSpan={columnas.length} className="px-4 py-8 text-center text-sm ui-text-muted">
                {sinDatos}
              </td>
            </tr>
          ) : (
            datos.map((fila, i) => (
              <tr
                key={i}
                className={
                  i % 2 === 0
                    ? 'bg-white dark:bg-dark-card'
                    : 'bg-slate-50/60 dark:bg-dark-elevated/50'
                }
              >
                {columnas.map((col) => (
                  <td key={col.key} className="border-b ui-border px-3.5 py-2.5 ui-text-body">
                    {col.render ? col.render(fila) : fila[col.key]}
                  </td>
                ))}
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}
