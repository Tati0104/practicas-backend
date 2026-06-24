import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { Badge, Button, Modal } from '@/shared/components/ui';
import estudianteService from '../services/estudianteService';
import seguimientoService from '@/modules/seguimiento/services/seguimientoService';
import { badgeEstadoPractica } from '@/modules/seguimiento/utils/estadosPractica';

const BADGE_SEGUIMIENTO = {
  AL_DIA: { label: 'Al día', variant: 'success' },
  PENDIENTE: { label: 'Pendiente', variant: 'warning' },
  EN_ALERTA: { label: 'En alerta', variant: 'danger' },
};

function normalizarPracticas(data) {
  if (Array.isArray(data)) return data;
  return data?.instanciasPractica ?? [];
}

export default function ModalExpedienteEstudiante({ estudiante, onCerrar, usarSeguimiento = true }) {
  const navigate = useNavigate();

  const { data: practicas = [], isLoading, isError } = useQuery({
    queryKey: ['expediente-estudiante', estudiante?.id, usarSeguimiento],
    queryFn: async () => {
      if (usarSeguimiento) {
        const resp = await seguimientoService.historialEstudiante(estudiante.id);
        return resp.data?.data ?? resp.data ?? [];
      }
      const resp = await estudianteService.obtenerExpediente(estudiante.id);
      const expediente = resp.data?.data ?? resp.data;
      return normalizarPracticas(expediente).map((p) => ({
        id: p.id,
        numeroPractica: p.numeroPractica,
        nombre: p.nombre,
        empresa: '—',
        docente: '—',
        estadoPractica: p.estado,
        estadoSeguimiento: null,
        fechaInicio: p.fechaInicio,
        fechaFin: p.fechaFin,
      }));
    },
    enabled: Boolean(estudiante?.id),
  });

  const practicasOrdenadas = [...practicas].sort(
    (a, b) => (a.numeroPractica ?? 0) - (b.numeroPractica ?? 0)
  );

  return (
    <Modal
      titulo="Historial de prácticas"
      onCerrar={onCerrar}
      ancho="max-w-4xl"
      acciones={
        <Button variant="secondary" size="sm" onClick={onCerrar}>
          Cerrar
        </Button>
      }
    >
      <p className="mb-4 text-sm text-gray-600">
        {estudiante?.nombre} · {estudiante?.identificacion ?? '—'}
        {estudiante?.programa?.nombre ? ` · ${estudiante.programa.nombre}` : ''}
      </p>

      {isLoading && <p className="text-sm text-gray-500">Cargando expediente…</p>}
      {isError && (
        <p className="text-sm text-red-700">No se pudo cargar el historial de prácticas.</p>
      )}

      {!isLoading && !isError && practicasOrdenadas.length === 0 && (
        <p className="rounded-lg border border-dashed border-gray-300 py-8 text-center text-sm text-gray-400">
          Este estudiante aún no tiene prácticas registradas en su expediente.
        </p>
      )}

      {!isLoading && practicasOrdenadas.length > 0 && (
        <div className="overflow-x-auto rounded-xl border border-gray-200">
          <table className="w-full border-collapse text-sm">
            <thead>
              <tr className="border-b border-gray-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-gray-600">
                <th className="px-3 py-2">Práctica</th>
                <th className="px-3 py-2">Empresa</th>
                <th className="px-3 py-2">Docente</th>
                <th className="px-3 py-2">Estado práctica</th>
                <th className="px-3 py-2">Seguimiento</th>
                <th className="px-3 py-2">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {practicasOrdenadas.map((p) => {
                const estadoPractica = badgeEstadoPractica(p.estadoPractica);
                const seguimiento =
                  BADGE_SEGUIMIENTO[p.estadoSeguimiento] ?? {
                    label: p.estadoSeguimiento?.replace(/_/g, ' ') ?? '—',
                    variant: 'neutral',
                  };
                return (
                  <tr key={p.id} className="border-b border-gray-100">
                    <td className="px-3 py-2">
                      <div className="font-semibold">Práctica {p.numeroPractica ?? '—'}</div>
                      {p.nombre && <div className="text-xs text-gray-500">{p.nombre}</div>}
                    </td>
                    <td className="px-3 py-2">{p.empresa ?? '—'}</td>
                    <td className="px-3 py-2">{p.docente ?? '—'}</td>
                    <td className="px-3 py-2">
                      <Badge variant={estadoPractica.variant}>{estadoPractica.label}</Badge>
                    </td>
                    <td className="px-3 py-2">
                      {p.estadoSeguimiento ? (
                        <Badge variant={seguimiento.variant}>{seguimiento.label}</Badge>
                      ) : (
                        '—'
                      )}
                    </td>
                    <td className="px-3 py-2">
                      <Button
                        size="sm"
                        variant="secondary"
                        onClick={() => {
                          onCerrar();
                          navigate(`/seguimiento/${p.id}`);
                        }}
                      >
                        Ver detalle
                      </Button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </Modal>
  );
}
