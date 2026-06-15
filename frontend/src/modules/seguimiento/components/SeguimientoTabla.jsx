// src/modules/seguimiento/components/SeguimientoTabla.jsx

import { Badge, Button } from '@/shared/components/ui';

const BADGE = {
  AL_DIA: { label: 'Al día', variant: 'success' },
  PENDIENTE: { label: 'Pendiente', variant: 'warning' },
  EN_ALERTA: { label: 'En alerta', variant: 'danger' },
};

export default function SeguimientoTabla({ practicas = [], onVerDetalle, etiquetaAccion = 'Ver' }) {
  return (
    <div className="overflow-x-auto rounded-xl border border-gray-200">
      <table className="w-full border-collapse text-sm">
        <thead>
          <tr className="border-b border-gray-200 bg-slate-50">
            {['Estudiante', 'Empresa / Cargo', 'Docente', 'Avance', 'Estado', 'Acciones'].map((h) => (
              <th
                key={h}
                className="px-3.5 py-2.5 text-left text-xs font-semibold uppercase tracking-wide text-gray-600"
              >
                {h}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {practicas.map((p) => {
            const badge = BADGE[p.estado] || { label: p.estado, variant: 'neutral' };
            return (
              <tr key={p.id} className="border-b border-gray-100 bg-white">
                <td className="px-3.5 py-3">
                  <div className="font-semibold text-gray-900">{p.estudiante?.nombre}</div>
                  <div className="text-xs text-gray-500">{p.estudiante?.codigo}</div>
                </td>
                <td className="px-3.5 py-3">
                  <div className="font-medium text-gray-800">{p.cargo}</div>
                  <div className="text-xs text-gray-500">{p.empresa}</div>
                </td>
                <td className="px-3.5 py-3 text-gray-700">{p.docente}</td>
                <td className="min-w-[120px] px-3.5 py-3">
                  <div className="mb-1 h-1.5 overflow-hidden rounded-full bg-gray-200">
                    <div
                      className="h-full rounded-full bg-primary"
                      style={{ width: `${p.porcentajeAvance || 0}%` }}
                    />
                  </div>
                  <div className="text-xs text-gray-500">{p.porcentajeAvance || 0}%</div>
                </td>
                <td className="px-3.5 py-3">
                  <Badge variant={badge.variant}>{badge.label}</Badge>
                </td>
                <td className="px-3.5 py-3">
                  <Button size="sm" onClick={() => onVerDetalle(p.id)}>
                    {etiquetaAccion}
                  </Button>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
