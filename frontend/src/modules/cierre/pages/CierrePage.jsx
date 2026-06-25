import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Lock } from 'lucide-react';
import useAuthStore from '@/store/authStore';
import { usePermisos } from '@/shared/hooks/usePermisos';
import { resolverIdPractica } from '@/modules/seguimiento/utils/practicaId';
import { extraerMensajeError } from '@/modules/calificaciones/utils/schemas';
import useCierre from '../hooks/useCierre';
import useCierreMutaciones from '../hooks/useCierreMutaciones';
import ChecklistCierre from '../components/ChecklistCierre';
import ProgresoCierre from '../components/ProgresoCierre';
import ConfirmarCierreModal from '../components/ConfirmarCierreModal';
import { Button, ErrorState, LoadingState, PageBackHeader } from '@/shared/components/ui';

export default function CierrePage() {
  const { practicaId: practicaIdParam } = useParams();
  const practicaId = resolverIdPractica(practicaIdParam);
  const navigate = useNavigate();
  const rol = useAuthStore((state) => state.rol);
  const { canClose } = usePermisos();

  const [modalAbierto, setModalAbierto] = useState(false);

  const {
    data: checklist,
    isLoading,
    isError,
    error,
    refetch,
  } = useCierre(practicaId);

  const { ejecutarCierre, enviarRecordatorio } = useCierreMutaciones(practicaId);

  const puedeGestionar = rol === 'COORD_PRACTICA' || rol === 'ADMIN';
  const puedeVerSoloLectura = rol === 'SECRETARIA';
  const puedeEjecutar = puedeGestionar && canClose;
  const puedeRecordatorio = puedeGestionar;

  if (!puedeGestionar && !puedeVerSoloLectura) {
    return (
      <div className="p-4 sm:p-6">
        <ErrorState mensaje="No tienes permiso para acceder al cierre de esta práctica." />
      </div>
    );
  }

  if (!practicaId) {
    return (
      <div className="p-4 sm:p-6">
        <ErrorState
          mensaje="Identificador de práctica inválido. Vuelve al listado y selecciona un estudiante de nuevo."
          onReintentar={() => navigate('/cierre')}
        />
      </div>
    );
  }

  if (isLoading) {
    return <LoadingState mensaje="Cargando checklist de cierre..." />;
  }

  if (isError) {
    return (
      <div className="p-4 sm:p-6">
        <ErrorState
          mensaje={extraerMensajeError(error, 'Error al cargar el checklist de cierre')}
          onReintentar={refetch}
        />
      </div>
    );
  }

  if (!checklist) {
    return (
      <div className="p-4 sm:p-6">
        <div className="rounded-xl border border-dashed border-gray-300 bg-gray-50 p-8 text-center text-sm text-gray-600">
          No se encontró información de cierre para esta práctica.
        </div>
      </div>
    );
  }

  const handleEjecutarCierre = () => {
    ejecutarCierre.mutate(undefined, {
      onSuccess: (response) => {
        setModalAbierto(false);
        const resultado = response?.data?.data;
        navigate(`/evaluaciones/${practicaId}?activa=false&archivada=true`, {
          replace: true,
          state: {
            cierreEjecutado: true,
            resultado: resultado?.resultado ?? null,
            aprobada: resultado?.aprobada ?? null,
          },
        });
      },
    });
  };

  const handleRecordatorio = (tipo) => {
    if (!tipo) return;
    enviarRecordatorio.mutate(tipo);
  };

  return (
    <div className="space-y-6 p-4 sm:p-6">
      <PageBackHeader
        titulo="Cierre de práctica"
        descripcion={`Práctica #${practicaId}`}
        onVolver={() => navigate(-1)}
        acciones={
          puedeEjecutar ? (
            <Button
              onClick={() => setModalAbierto(true)}
              disabled={!checklist.habilitarBotonCierre || ejecutarCierre.isPending}
              title={
                checklist.habilitarBotonCierre
                  ? 'Ejecutar cierre oficial de la práctica'
                  : 'Completa todos los requisitos obligatorios para habilitar el cierre'
              }
            >
              <Lock className="h-4 w-4" aria-hidden="true" />
              Ejecutar cierre
            </Button>
          ) : null
        }
      />

      {puedeVerSoloLectura && (
        <div className="rounded-xl border border-blue-200 bg-blue-50 px-4 py-3 text-sm text-blue-800">
          {rol === 'ESTUDIANTE'
            ? 'Estás viendo el progreso de cierre de tu práctica en modo solo lectura. La ejecución del cierre la realiza Coordinación de Prácticas.'
            : 'Estás en modo de solo lectura. La ejecución del cierre y el envío de recordatorios los realiza Coordinación de Prácticas.'}
        </div>
      )}

      <ProgresoCierre
        itemsCompletados={checklist.itemsCompletados}
        totalItems={checklist.totalItems}
        progresoGlobal={checklist.progresoGlobal}
      />

      {!checklist.habilitarBotonCierre && checklist.itemsPendientes > 0 && (
        <div className="rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800">
          Faltan {checklist.itemsPendientes} requisito(s) obligatorio(s) por completar antes de
          ejecutar el cierre.
        </div>
      )}

      <section className="space-y-4">
        <h2 className="text-lg font-semibold text-gray-900">Checklist de requisitos</h2>
        <ChecklistCierre
          checklist={checklist}
          puedeEnviarRecordatorio={puedeRecordatorio}
          recordatorioPendiente={enviarRecordatorio.isPending}
          onRecordatorio={handleRecordatorio}
        />
      </section>

      <ConfirmarCierreModal
        isOpen={modalAbierto}
        onClose={() => setModalAbierto(false)}
        onConfirm={handleEjecutarCierre}
        isPending={ejecutarCierre.isPending}
      />
    </div>
  );
}
