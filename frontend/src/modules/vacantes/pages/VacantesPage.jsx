// src/modules/vacantes/pages/VacantesPage.jsx

import { useEffect, useState } from 'react';
import { toast } from 'react-hot-toast';
import { useVacantes } from '../hooks/useVacantes';
import { useVacantesMutaciones } from '../hooks/useVacantesMutaciones';
import VacantesFiltros from '../components/VacantesFiltros';
import VacantesTabla from '../components/VacantesTabla';
import VacanteCard from '../components/VacanteCard';
import VacanteForm from '../components/VacanteForm';
import { usePermisos } from '../../../shared/hooks/usePermisos';
import useAuthStore from '@/store/authStore';
import Paginacion from '../../../shared/components/Paginacion';
import { Button, PageHeader } from '@/shared/components/ui';

function useEsDesktop() {
  const [esDesktop, setEsDesktop] = useState(() =>
    window.matchMedia('(min-width: 1280px)').matches
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
  const { vacantes, isLoading, isError, error, filtros, setFiltros, totalPaginas, irAPagina, refetch } =
    useVacantes();
  const rol = useAuthStore((state) => state.rol);
  const { aprobar, rechazar, pausar, reanudar, cerrar } = useVacantesMutaciones({
    onSuccess: () => {
      toast.success('Operación exitosa');
      refetch();
    },
    onError: (err) => {
      toast.error(err?.message || 'Error en la operación');
    },
  });

  const isDesktop = useEsDesktop();
  const { canCreate, canApprove, canReject, canPause, canClose } = usePermisos();
  const [formAbierto, setFormAbierto] = useState(false);
  const [vacanteEditando, setVacanteEditando] = useState(null);

  useEffect(() => {
    if (isError) {
      toast.error(error?.response?.data?.message ?? error?.message ?? 'Error al cargar vacantes');
    }
  }, [isError, error]);

  const abrirCrear = () => {
    setVacanteEditando(null);
    setFormAbierto(true);
  };
  const cerrarForm = () => {
    setFormAbierto(false);
    setVacanteEditando(null);
    refetch();
  };

  const acciones = { aprobar, rechazar, pausar, reanudar, cerrar, canApprove, canReject, canPause, canClose };

  return (
    <div>
      <PageHeader
        titulo="Vacantes"
        descripcion="Gestión de vacantes y postulaciones"
        acciones={
          canCreate ? <Button onClick={abrirCrear}>+ Nueva vacante</Button> : null
        }
      />

      <VacantesFiltros filtros={filtros} setFiltros={setFiltros} />

      {isLoading && (
        <p className="py-10 text-center text-sm text-gray-500">Cargando vacantes...</p>
      )}

      {!isLoading && !isError && vacantes.length === 0 && (
        <div className="rounded-lg border border-dashed border-gray-300 py-12 text-center text-gray-400">
          {rol === 'EMPRESA' ? 'No tienes vacantes registradas.' : 'No hay vacantes registradas.'}
        </div>
      )}

      {!isLoading && isError && (
        <div className="rounded-lg border border-red-200 bg-red-50 py-12 text-center text-sm text-red-700">
          {error?.response?.data?.message ?? error?.message ?? 'No se pudieron cargar las vacantes.'}
        </div>
      )}

      {!isLoading && vacantes.length > 0 && (
        isDesktop ? (
          <VacantesTabla vacantes={vacantes} acciones={acciones} />
        ) : (
          <div className="grid gap-3">
            {vacantes.map((v) => (
              <VacanteCard key={v.id} vacante={v} acciones={acciones} />
            ))}
          </div>
        )
      )}

      <Paginacion
        pagina={filtros.page}
        totalPaginas={totalPaginas}
        onCambiarPagina={irAPagina}
      />

      <VacanteForm isOpen={formAbierto} onClose={cerrarForm} vacante={vacanteEditando} />
    </div>
  );
}
