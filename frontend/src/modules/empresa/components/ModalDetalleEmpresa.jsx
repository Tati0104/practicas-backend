import { useQuery } from '@tanstack/react-query';
import empresaService from '../services/empresaService';
import TablaBase from '../../../shared/components/TablaBase';
import BadgeEstado from '../../../shared/components/BadgeEstado';
import { Badge, Button, LoadingState, Modal } from '@/shared/components/ui';

function formatearFecha(fecha) {
  if (!fecha) return '—';
  try {
    return new Date(fecha).toLocaleString('es-CO', {
      dateStyle: 'medium',
      timeStyle: 'short',
    });
  } catch {
    return fecha;
  }
}

export default function ModalDetalleEmpresa({
  empresa,
  onCerrar,
  onInactivar,
  onActivar,
  activando = false,
}) {
  const { data: tutores = [], isLoading } = useQuery({
    queryKey: ['empresa-tutores', empresa?.id],
    queryFn: () => empresaService.listarTutores(empresa.id).then((r) => r.data ?? []),
    enabled: Boolean(empresa?.id),
  });

  const columnasTutores = [
    { key: 'nombre', titulo: 'Nombre' },
    { key: 'cargo', titulo: 'Cargo' },
    { key: 'correo', titulo: 'Correo' },
    { key: 'telefono', titulo: 'Teléfono' },
    {
      key: 'activo',
      titulo: 'Estado',
      render: (t) => <BadgeEstado activo={t.activo} />,
    },
  ];

  return (
    <Modal
      titulo="Detalle de empresa"
      onCerrar={onCerrar}
      ancho="max-w-3xl"
      acciones={
        <div className="flex flex-wrap justify-end gap-2">
          <Button variant="secondary" size="sm" onClick={onCerrar}>
            Cerrar
          </Button>
          {empresa?.activo ? (
            <Button variant="danger" size="sm" onClick={() => onInactivar(empresa)}>
              Inactivar empresa
            </Button>
          ) : (
            <Button size="sm" onClick={() => onActivar(empresa)} disabled={activando}>
              {activando ? 'Activando…' : 'Reactivar empresa'}
            </Button>
          )}
        </div>
      }
    >
      <div className="space-y-4">
        <div className="grid grid-cols-1 gap-3 rounded-lg border border-gray-200 bg-slate-50 p-4 sm:grid-cols-2">
          <div>
            <p className="text-xs font-semibold uppercase text-gray-500">Razón social</p>
            <p className="text-sm font-medium text-gray-900">{empresa?.razonSocial}</p>
          </div>
          <div>
            <p className="text-xs font-semibold uppercase text-gray-500">NIT</p>
            <p className="text-sm text-gray-800">{empresa?.nit}</p>
          </div>
          <div>
            <p className="text-xs font-semibold uppercase text-gray-500">Sector</p>
            <Badge variant="purple">{empresa?.sector?.nombre ?? '—'}</Badge>
          </div>
          <div>
            <p className="text-xs font-semibold uppercase text-gray-500">Estado</p>
            <BadgeEstado activo={empresa?.activo} />
          </div>
          <div>
            <p className="text-xs font-semibold uppercase text-gray-500">Municipio</p>
            <p className="text-sm text-gray-800">{empresa?.municipio || '—'}</p>
          </div>
          <div>
            <p className="text-xs font-semibold uppercase text-gray-500">Teléfono</p>
            <p className="text-sm text-gray-800">{empresa?.telefono || '—'}</p>
          </div>
        </div>

        {!empresa?.activo && empresa?.motivoInactivacion && (
          <div className="rounded-lg border border-amber-200 bg-amber-50 p-4">
            <p className="text-xs font-semibold uppercase text-amber-800">Motivo de inactivación</p>
            <p className="mt-1 text-sm text-amber-900">{empresa.motivoInactivacion}</p>
            {empresa.fechaInactivacion && (
              <p className="mt-2 text-xs text-amber-700">
                Inactivada el {formatearFecha(empresa.fechaInactivacion)}
              </p>
            )}
          </div>
        )}

        <div>
          <div className="mb-2 flex items-center justify-between">
            <h4 className="text-sm font-semibold text-gray-900">
              Tutores empresariales ({tutores.length})
            </h4>
          </div>

          {isLoading ? (
            <LoadingState mensaje="Cargando tutores…" />
          ) : tutores.length === 0 ? (
            <p className="rounded-lg border border-dashed border-gray-300 py-6 text-center text-sm text-gray-400">
              Esta empresa no tiene tutores empresariales registrados.
            </p>
          ) : (
            <div className="overflow-x-auto rounded-lg border border-gray-200">
              <TablaBase columnas={columnasTutores} datos={tutores} />
            </div>
          )}
        </div>
      </div>
    </Modal>
  );
}
