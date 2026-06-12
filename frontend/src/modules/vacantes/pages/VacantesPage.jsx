// src/modules/vacantes/pages/VacantesPage.jsx

/**
 * Página principal del módulo Vacantes.
 * - Muestra la barra de filtros.
 * - En desktop renderiza una tabla reutilizando TablaBase.
 * - En mobile renderiza tarjetas individuales.
 * - Utiliza los hooks useVacantes y useVacantesMutaciones.
 * - Controla permisos mediante usePermisos.
 */
import { useEffect, useState } from 'react';
import { useVacantes } from '../hooks/useVacantes';
import { useVacantesMutaciones } from '../hooks/useVacantesMutaciones';
import VacantesFiltros from '../components/VacantesFiltros';
import VacantesTabla from '../components/VacantesTabla';
import VacanteCard from '../components/VacanteCard';
import { usePermisos } from '../../../shared/hooks/usePermisos';
import { toast } from 'react-hot-toast';

// Hook nativo para detectar breakpoint sin dependencia extra.
// Escucha cambios en tiempo real y limpia el listener al desmontar.
function useEsDesktop() {
  const [esDesktop, setEsDesktop] = useState(
    () => window.matchMedia('(min-width: 1280px)').matches
  );
  useEffect(() => {
    const mq = window.matchMedia('(min-width: 1280px)');
    const handler = (e) => setEsDesktop(e.matches);
    mq.addEventListener('change', handler);
    return () => mq.removeEventListener('change', handler);
  }, []);
  return esDesktop;
}


export default function VacantesPage() {
  const { vacantes, isLoading, isError, filtros, setFiltros, refetch } = useVacantes();
  const { crear, editar, aprobar, rechazar, pausar, cerrar } = useVacantesMutaciones({ onSuccess: () => {
    toast.success('Operación exitosa');
    refetch();
  }, onError: (err) => {
    toast.error(err?.message || 'Error en la operación');
  } });

  const isDesktop = useEsDesktop(); // usa el hook nativo definido arriba
  const { canCreate, canEdit, canApprove, canReject, canPause, canClose } = usePermisos();

  // Si ocurre un error, podemos mostrar un toast (también manejado por UI externa).
  useEffect(() => {
    if (isError) toast.error('Error al cargar vacantes');
  }, [isError]);

  return (
    <div className="p-4">
      <h1 className="text-2xl font-bold mb-4 text-gray-800">Vacantes</h1>
      <VacantesFiltros filtros={filtros} setFiltros={setFiltros} />
      {isLoading && (<div className="flex justify-center py-8"><span className="loader"/></div>)}
      {!isLoading && vacantes.length === 0 && (
        <div className="text-center py-8 text-gray-500">No hay vacantes registradas.</div>
      )}
      {!isLoading && vacantes.length > 0 && (
        isDesktop ? (
          <VacantesTabla vacantes={vacantes} acciones={{ aprobar, rechazar, pausar, cerrar, canApprove, canReject, canPause, canClose }} />
        ) : (
          <div className="grid gap-4">
            {vacantes.map(v => (
              <VacanteCard key={v.id} vacante={v} acciones={{ aprobar, rechazar, pausar, cerrar, canApprove, canReject, canPause, canClose }} />
            ))}
          </div>
        )
      )}
    </div>
  );
}
