import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Loader2, Lock, RefreshCw } from 'lucide-react';
import useAuthStore from '@/store/authStore';
import { usePermisos } from '@/shared/hooks/usePermisos';
import { extraerMensajeError } from '@/modules/calificaciones/utils/schemas';
import useCierre from '../hooks/useCierre';
import useCierreMutaciones from '../hooks/useCierreMutaciones';
import ChecklistCierre from '../components/ChecklistCierre';
import ProgresoCierre from '../components/ProgresoCierre';
import ConfirmarCierreModal from '../components/ConfirmarCierreModal';

function Spinner({ mensaje }) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 p-12 text-gray-600">
      <Loader2 className="h-8 w-8 animate-spin text-blue-700" aria-hidden="true" />
      <p className="text-sm">{mensaje}</p>
    </div>
  );
}

function ErrorEstado({ mensaje, onReintentar }) {
  return (
    <div className="rounded-xl border border-red-200 bg-red-50 p-6 text-center">
      <p className="text-sm text-red-700" role="alert">
        {mensaje}
      </p>
      {onReintentar && (
        <button
          type="button"
          onClick={onReintentar}
          className="mt-4 inline-flex items-center gap-2 rounded-lg bg-red-600 px-4 py-2 text-sm font-semibold text-white hover:bg-red-700"
        >
          <RefreshCw className="h-4 w-4" aria-hidden="true" />
          Reintentar
        </button>
      )}
    </div>
  );
}

export default function CierrePage() {
  const { practicaId } = useParams();
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
  const puedeEjecutar = puedeGestionar && canClose;
  const puedeRecordatorio = puedeGestionar;

  if (!puedeGestionar) {
    return (
      <div className="p-4 sm:p-6">
        <ErrorEstado mensaje="No tienes permiso para acceder al cierre de esta práctica." />
      </div>
    );
  }

  if (isLoading) {
    return <Spinner mensaje="Cargando checklist de cierre..." />;
  }

  if (isError) {
    return (
      <div className="p-4 sm:p-6">
        <ErrorEstado
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
        navigate(`/calificaciones/${practicaId}?activa=false&archivada=true`, {
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
      <header className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <button
            type="button"
            onClick={() => navigate(-1)}
            className="mb-2 inline-flex items-center gap-1 text-sm font-medium text-blue-700 hover:underline"
          >
            <ArrowLeft className="h-4 w-4" aria-hidden="true" />
            Volver
          </button>
          <h1 className="text-2xl font-bold text-gray-900">Cierre de práctica</h1>
          <p className="text-sm text-gray-500">Práctica #{practicaId}</p>
        </div>

        {puedeEjecutar && (
          <button
            type="button"
            onClick={() => setModalAbierto(true)}
            disabled={!checklist.habilitarBotonCierre || ejecutarCierre.isPending}
            className="inline-flex items-center justify-center gap-2 rounded-lg bg-blue-700 px-4 py-2.5 text-sm font-semibold text-white hover:bg-blue-800 disabled:cursor-not-allowed disabled:bg-gray-400"
            title={
              checklist.habilitarBotonCierre
                ? 'Ejecutar cierre oficial de la práctica'
                : 'Completa todos los requisitos obligatorios para habilitar el cierre'
            }
          >
            <Lock className="h-4 w-4" aria-hidden="true" />
            Ejecutar cierre
          </button>
        )}
      </header>

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
