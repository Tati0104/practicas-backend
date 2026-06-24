import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { Badge, Button, Modal } from '@/shared/components/ui';
import estudianteService from '../services/estudianteService';
import { ESTADO_PRACTICA } from '@/modules/seguimiento/utils/estadosPractica';

function badgeEstado(estado) {
  return (
    ESTADO_PRACTICA[estado] ?? {
      label: estado?.replace(/_/g, ' ') ?? 'Sin estado',
      variant: 'neutral',
    }
  );
}

export default function ModalExpedienteEstudiante({ estudiante, onCerrar }) {
  const navigate = useNavigate();
  const { data: expediente, isLoading, isError } = useQuery({
    queryKey: ['expediente-estudiante', estudiante?.id],
    queryFn: () => estudianteService.obtenerExpediente(estudiante.id).then((r) => r.data),
    enabled: Boolean(estudiante?.id),
  });

  const practicas = [...(expediente?.instanciasPractica ?? [])].sort(
    (a, b) => (a.numeroPractica ?? 0) - (b.numeroPractica ?? 0)
  );

  return (
    <Modal
      titulo="Historial de prácticas"
      onCerrar={onCerrar}
      ancho="max-w-2xl"
      acciones={
        <Button variant="secondary" size="sm" onClick={onCerrar}>
          Cerrar
        </Button>
      }
    >
      <p className="mb-4 text-sm text-gray-600">
        {estudiante?.nombre} · {estudiante?.identificacion} ·{' '}
        {estudiante?.programa?.nombre ?? 'Programa no indicado'}
      </p>

      {isLoading && <p className="text-sm text-gray-500">Cargando expediente…</p>}
      {isError && (
        <p className="text-sm text-red-700">No se pudo cargar el historial de prácticas.</p>
      )}

      {!isLoading && !isError && practicas.length === 0 && (
        <p className="rounded-lg border border-dashed border-gray-300 py-8 text-center text-sm text-gray-400">
          Este estudiante aún no tiene prácticas registradas en su expediente.
        </p>
      )}

      {!isLoading && practicas.length > 0 && (
        <div className="overflow-x-auto rounded-xl border border-gray-200">
          <table className="w-full border-collapse text-sm">
            <thead>
              <tr className="border-b border-gray-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-gray-600">
                <th className="px-3 py-2">Práctica</th>
                <th className="px-3 py-2">Nombre</th>
                <th className="px-3 py-2">Estado</th>
                <th className="px-3 py-2">Inicio</th>
                <th className="px-3 py-2">Fin</th>
                <th className="px-3 py-2">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {practicas.map((p) => {
                const estado = badgeEstado(p.estado);
                return (
                  <tr key={p.id} className="border-b border-gray-100">
                    <td className="px-3 py-2 font-semibold">{p.numeroPractica ?? '—'}</td>
                    <td className="px-3 py-2">{p.nombre ?? '—'}</td>
                    <td className="px-3 py-2">
                      <Badge variant={estado.variant}>{estado.label}</Badge>
                    </td>
                    <td className="px-3 py-2">{p.fechaInicio ?? '—'}</td>
                    <td className="px-3 py-2">{p.fechaFin ?? '—'}</td>
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
