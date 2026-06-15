export default function TablaBase({ columnas, datos, cargando, sinDatos = 'No hay registros' }) {
  if (cargando) {
    return <p className="text-sm text-gray-500">Cargando...</p>;
  }

  return (
    <div className="overflow-x-auto rounded-lg border border-gray-200">
      <table className="w-full border-collapse text-sm">
        <thead>
          <tr className="border-b border-gray-200 bg-slate-50">
            {columnas.map((col) => (
              <th
                key={col.key}
                className="px-3.5 py-2.5 text-left text-xs font-semibold uppercase tracking-wide text-gray-600"
              >
                {col.titulo}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {datos.length === 0 ? (
            <tr>
              <td colSpan={columnas.length} className="px-4 py-8 text-center text-sm text-gray-400">
                {sinDatos}
              </td>
            </tr>
          ) : (
            datos.map((fila, i) => (
              <tr
                key={i}
                className={i % 2 === 0 ? 'bg-white' : 'bg-slate-50/60'}
              >
                {columnas.map((col) => (
                  <td key={col.key} className="border-b border-gray-100 px-3.5 py-2.5 text-gray-700">
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
