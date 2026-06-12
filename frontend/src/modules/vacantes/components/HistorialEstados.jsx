// src/modules/vacantes/components/HistorialEstados.jsx

/**
 * Historial de estados de una vacante.
 * Recibe la prop `historial` que es un arreglo de objetos con la forma:
 *   { estado: string, fecha: string }
 * Cada estado se muestra con un badge (color definido en `colorEstado`).
 * Se renderiza una tabla simple en desktop y una lista en mobile.
 */
import React from 'react';
// BadgeEstado del shared solo acepta `activo: boolean`, por eso este
// componente usa sus propios estilos en línea con el mapa colorEstado.

// Colores reutilizados del mismo mapa que usa VacanteCard.
const colorEstado = {
  ACTIVA:               { bg: '#d1fae5', color: '#065f46' },
  PENDIENTE_APROBACION:{ bg: '#fef3c7', color: '#92400e' },
  PAUSADA:              { bg: '#f3f4f6', color: '#374151' },
  CUPOS_COMPLETOS:      { bg: '#fed7aa', color: '#92400e' },
  CERRADA:              { bg: '#fee2e2', color: '#991b1b' },
};

export default function HistorialEstados({ historial }) {
  if (!historial || historial.length === 0) {
    return (
      <div className="text-center py-4 text-gray-500">
        No hay historial de estados.
      </div>
    );
  }

  return (
    <div className="mt-4">
      <h2 className="text-lg font-medium mb-2 text-gray-800">Historial de estados</h2>
      <div className="overflow-x-auto">
        <table className="min-w-full border border-gray-200">
          <thead className="bg-gray-100">
            <tr>
              <th className="px-4 py-2 text-left">Fecha</th>
              <th className="px-4 py-2 text-left">Estado</th>
            </tr>
          </thead>
          <tbody>
            {historial.map((h, idx) => {
              const c = colorEstado[h.estado] || colorEstado.CERRADA;
              return (
                <tr key={idx} className={idx % 2 === 0 ? 'bg-white' : 'bg-gray-50'}>
                  <td className="px-4 py-2 whitespace-nowrap">
                    {new Date(h.fecha).toLocaleDateString()} {new Date(h.fecha).toLocaleTimeString()}
                  </td>
                  <td className="px-4 py-2 whitespace-nowrap">
                    <span
                      style={{
                        background: c.bg,
                        color: c.color,
                        padding: '2px 8px',
                        borderRadius: '12px',
                        fontSize: '0.85rem',
                        fontWeight: 600,
                      }}
                    >
                      {h.estado.replace('_', ' ')}
                    </span>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}
