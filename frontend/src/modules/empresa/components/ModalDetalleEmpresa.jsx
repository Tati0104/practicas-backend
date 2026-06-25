import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { UserPlus } from 'lucide-react';
import empresaService from '../services/empresaService';
import ModalRegistrarTutor from './ModalRegistrarTutor';
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
  onRegistrarTutor,
  registrandoTutor = false,
  activando = false,
}) {
  const [modalTutorOpen, setModalTutorOpen] = useState(false);

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

  const handleRegistrarTutor = (dto) => {
    onRegistrarTutor?.(dto, {
      onSuccess: () => setModalTutorOpen(false),
    });
  };

  return (
    <>
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
          <div className="grid grid-cols-1 gap-3 rounded-lg border ui-border bg-slate-50 p-4 dark:bg-dark-elevated sm:grid-cols-2">
            <div>
              <p className="text-xs font-semibold uppercase ui-text-muted">Razón social</p>
              <p className="text-sm font-medium ui-text-title">{empresa?.razonSocial}</p>
            </div>
            <div>
              <p className="text-xs font-semibold uppercase ui-text-muted">NIT</p>
              <p className="text-sm ui-text-body">{empresa?.nit}</p>
            </div>
            <div>
              <p className="text-xs font-semibold uppercase ui-text-muted">Sector</p>
              <Badge variant="purple">{empresa?.sector?.nombre ?? '—'}</Badge>
            </div>
            <div>
              <p className="text-xs font-semibold uppercase ui-text-muted">Estado</p>
              <BadgeEstado activo={empresa?.activo} />
            </div>
            <div>
              <p className="text-xs font-semibold uppercase ui-text-muted">Municipio</p>
              <p className="text-sm ui-text-body">{empresa?.municipio || '—'}</p>
            </div>
            <div>
              <p className="text-xs font-semibold uppercase ui-text-muted">Teléfono</p>
              <p className="text-sm ui-text-body">{empresa?.telefono || '—'}</p>
            </div>
          </div>

          {!empresa?.activo && empresa?.motivoInactivacion && (
            <div className="rounded-lg border border-amber-200 bg-amber-50 p-4 dark:border-amber-500/30 dark:bg-amber-500/10">
              <p className="text-xs font-semibold uppercase text-amber-800 dark:text-amber-300">
                Motivo de inactivación
              </p>
              <p className="mt-1 text-sm text-amber-900 dark:text-amber-100">{empresa.motivoInactivacion}</p>
              {empresa.fechaInactivacion && (
                <p className="mt-2 text-xs text-amber-700 dark:text-amber-200">
                  Inactivada el {formatearFecha(empresa.fechaInactivacion)}
                </p>
              )}
            </div>
          )}

          <div>
            <div className="mb-2 flex flex-wrap items-center justify-between gap-2">
              <h4 className="text-sm font-semibold ui-text-title">
                Tutores empresariales ({tutores.length})
              </h4>
              {empresa?.activo && (
                <Button size="sm" variant="secondary" onClick={() => setModalTutorOpen(true)}>
                  <UserPlus className="h-4 w-4" aria-hidden="true" />
                  Agregar tutor
                </Button>
              )}
            </div>

            {!empresa?.activo && (
              <p className="mb-3 text-xs ui-text-muted">
                Reactive la empresa para registrar tutores empresariales.
              </p>
            )}

            {isLoading ? (
              <LoadingState mensaje="Cargando tutores…" />
            ) : tutores.length === 0 ? (
              <div className="rounded-lg border border-dashed ui-border py-8 text-center">
                <p className="text-sm ui-text-muted">
                  Esta empresa no tiene tutores empresariales registrados.
                </p>
                {empresa?.activo && (
                  <Button
                    className="mt-3"
                    size="sm"
                    onClick={() => setModalTutorOpen(true)}
                  >
                    <UserPlus className="h-4 w-4" aria-hidden="true" />
                    Agregar primer tutor
                  </Button>
                )}
              </div>
            ) : (
              <div className="overflow-x-auto rounded-lg border ui-border">
                <TablaBase columnas={columnasTutores} datos={tutores} />
              </div>
            )}
          </div>
        </div>
      </Modal>

      {modalTutorOpen && empresa?.activo && (
        <ModalRegistrarTutor
          empresa={empresa}
          guardando={registrandoTutor}
          onCerrar={() => setModalTutorOpen(false)}
          onGuardar={handleRegistrarTutor}
        />
      )}
    </>
  );
}
