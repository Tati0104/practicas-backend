import { Badge, Button } from '@/shared/components/ui';
import {
  estadoSeguimientoPractica,
  formatearFechaSeguimiento,
  nombreEstudiantePractica,
} from '../utils/fechas';
import { resolverIdPractica } from '../utils/practicaId';
import { badgeEstadoPractica } from '../utils/estadosPractica';

const BADGE = {
  AL_DIA: { label: 'Al día', variant: 'success' },
  PENDIENTE: { label: 'Pendiente', variant: 'warning' },
  EN_ALERTA: { label: 'En alerta', variant: 'danger' },
};

export default function SeguimientoTabla({
  practicas = [],
  onVerDetalle,
  etiquetaAccion = 'Ver',
  ocultarEstudiante = false,
  mostrarEstadoPractica = false,
}) {
  const columnas = ocultarEstudiante
    ? ['Práctica', 'Empresa / Cargo', 'Docente', 'Corte', 'Estado', 'Última actividad', 'Acciones']
    : mostrarEstadoPractica
      ? [
          'Estudiante',
          'Empresa / Cargo',
          'Docente',
          'Práctica',
          'Estado práctica',
          'Seguimiento',
          'Última actividad',
          'Acciones',
        ]
      : ['Estudiante', 'Empresa / Cargo', 'Docente', 'Corte', 'Estado', 'Última actividad', 'Acciones'];

  return (
    <div className="overflow-x-auto rounded-xl border border-gray-200">
      <table className="w-full border-collapse text-sm">
        <thead>
          <tr className="border-b border-gray-200 bg-slate-50">
            {columnas.map((h) => (
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
            const estadoClave = estadoSeguimientoPractica(p);
            const badge = BADGE[estadoClave] || { label: estadoClave, variant: 'neutral' };
            const estadoPracticaBadge = badgeEstadoPractica(p.estadoPractica);
            const fechaStr = formatearFechaSeguimiento(p.fechaUltimaActividad);
            const codigo =
              typeof p.estudiante === 'object' ? p.estudiante?.codigo : null;

            return (
              <tr key={p.estudianteId ?? p.id} className="border-b border-gray-100 bg-white">
                <td className="px-3.5 py-3">
                  <div className="font-semibold text-gray-900">
                    {ocultarEstudiante
                      ? `Práctica ${p.numeroPractica ?? '—'}`
                      : nombreEstudiantePractica(p)}
                  </div>
                  {ocultarEstudiante && p.estadoPractica && (
                    <div className="text-xs text-gray-500">{p.estadoPractica.replace(/_/g, ' ')}</div>
                  )}
                  {!ocultarEstudiante && (p.identificacion || codigo) && (
                    <div className="text-xs text-gray-500">{p.identificacion || codigo}</div>
                  )}
                </td>
                <td className="px-3.5 py-3">
                  <div className="font-medium text-gray-800">{p.cargo || '—'}</div>
                  <div className="text-xs text-gray-500">{p.empresa || '—'}</div>
                </td>
                <td className="px-3.5 py-3 text-gray-700">{p.docente || '—'}</td>
                {mostrarEstadoPractica ? (
                  <>
                    <td className="px-3.5 py-3 text-center text-gray-700">
                      {p.cantidadPracticas > 1
                        ? `${p.cantidadPracticas} prácticas`
                        : (p.numeroPractica ?? '—')}
                    </td>
                    <td className="px-3.5 py-3">
                      <Badge variant={estadoPracticaBadge.variant}>
                        {estadoPracticaBadge.label}
                      </Badge>
                    </td>
                    <td className="px-3.5 py-3">
                      <Badge variant={badge.variant}>{badge.label}</Badge>
                    </td>
                  </>
                ) : (
                  <>
                    <td className="px-3.5 py-3 text-center text-gray-700">{p.corte ?? '—'}</td>
                    <td className="px-3.5 py-3">
                      <Badge variant={badge.variant}>{badge.label}</Badge>
                    </td>
                  </>
                )}
                <td className="px-3.5 py-3 text-xs text-gray-500">{fechaStr}</td>
                <td className="px-3.5 py-3">
                  <Button
                    size="sm"
                    onClick={() => {
                      const id = resolverIdPractica(p);
                      onVerDetalle(id ?? p);
                    }}
                  >
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
