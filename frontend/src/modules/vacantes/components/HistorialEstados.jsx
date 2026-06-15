// src/modules/vacantes/components/HistorialEstados.jsx

import BadgeVacanteEstado from './BadgeVacanteEstado';

export default function HistorialEstados({ historial }) {
  if (!historial || historial.length === 0) {
    return (
      <div className="py-4 text-center text-sm text-gray-500">
        No hay historial de estados.
      </div>
    );
  }

  return (
    <div className="mt-4">
      <h2 className="mb-2 text-base font-semibold text-gray-800">Historial de estados</h2>
      <div className="overflow-x-auto rounded-lg border border-gray-200">
        <table className="min-w-full text-sm">
          <thead className="bg-slate-50">
            <tr>
              <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-gray-600">Fecha</th>
              <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-gray-600">Estado</th>
            </tr>
          </thead>
          <tbody>
            {historial.map((h, idx) => (
              <tr key={idx} className={idx % 2 === 0 ? 'bg-white' : 'bg-slate-50/60'}>
                <td className="whitespace-nowrap px-4 py-2 text-gray-700">
                  {new Date(h.fecha).toLocaleDateString()}{' '}
                  {new Date(h.fecha).toLocaleTimeString()}
                </td>
                <td className="whitespace-nowrap px-4 py-2">
                  <BadgeVacanteEstado estado={h.estado} />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
