// src/modules/asignaciones/pages/AsignacionesPage.jsx

import { useState, useEffect } from 'react';
import { ClipboardList } from 'lucide-react';
import { toast } from 'react-hot-toast';
import { useAsignaciones } from '../hooks/useAsignaciones';
import { useAsignacionesMutaciones } from '../hooks/useAsignacionesMutaciones';
import { usePermisos } from '../../../shared/hooks/usePermisos';
import { usePuedeAsignarDocenteAsesor } from '../../../shared/hooks/usePermisosDocenteAsesor';
import AsignacionesFiltros from '../components/AsignacionesFiltros';
import AsignacionesTabla from '../components/AsignacionesTabla';
import AsignacionCard from '../components/AsignacionCard';
import AsignacionForm from '../components/AsignacionForm';
import CancelarAsignacionModal from '../components/CancelarAsignacionModal';
import AsignarDocenteModal from '../components/AsignarDocenteModal';
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

export default function AsignacionesPage() {
  const esDesktop = useEsDesktop();
  const [formAbierto, setFormAbierto] = useState(false);
  const [asignacionACancelar, setAsignacionACancelar] = useState(null);
  const [asignacionParaDocente, setAsignacionParaDocente] = useState(null);

  const { asignaciones, totalPaginas, isLoading, isError, filtros, setFiltros, irAPagina } =
    useAsignaciones();
  const { canCreate } = usePermisos();
  const puedeAsignarDocente = usePuedeAsignarDocenteAsesor();

  const { crear, cancelar, asignarDocente } = useAsignacionesMutaciones({
    onSuccess: () => {
      setFormAbierto(false);
      setAsignacionACancelar(null);
      setAsignacionParaDocente(null);
    },
  });

  useEffect(() => {
    if (isError) toast.error('Error al cargar asignaciones');
  }, [isError]);

  return (
    <div>
      <PageHeader
        titulo="Asignaciones"
        descripcion="Postulaciones y asignación de estudiantes a vacantes"
        acciones={
          canCreate ? (
            <Button onClick={() => setFormAbierto(true)}>+ Nueva asignación</Button>
          ) : null
        }
      />

      <AsignacionesFiltros filtros={filtros} setFiltros={setFiltros} />

      {isLoading && (
        <p className="py-10 text-center text-sm text-gray-500">Cargando asignaciones...</p>
      )}

      {!isLoading && asignaciones.length === 0 && (
        <div className="rounded-lg border border-dashed border-gray-300 py-12 text-center text-gray-400">
          <ClipboardList className="mx-auto mb-2 h-8 w-8 opacity-50" aria-hidden="true" />
          No hay asignaciones que coincidan con los filtros.
        </div>
      )}

      {!isLoading && asignaciones.length > 0 && esDesktop && (
        <AsignacionesTabla
          asignaciones={asignaciones}
          isLoading={isLoading}
          canCancelar={canCreate}
          onCancelar={(a) => setAsignacionACancelar(a)}
          canAsignarDocente={puedeAsignarDocente}
          onAsignarDocente={(a) => setAsignacionParaDocente(a)}
        />
      )}

      {!isLoading && asignaciones.length > 0 && !esDesktop && (
        <div className="flex flex-col gap-3">
          {asignaciones.map((a) => (
            <AsignacionCard
              key={a.id}
              asignacion={a}
              canCancelar={canCreate}
              onCancelar={(item) => setAsignacionACancelar(item)}
              canAsignarDocente={puedeAsignarDocente}
              onAsignarDocente={(item) => setAsignacionParaDocente(item)}
            />
          ))}
        </div>
      )}

      <Paginacion pagina={filtros.page} totalPaginas={totalPaginas} onCambiarPagina={irAPagina} />

      <AsignacionForm
        isOpen={formAbierto}
        onClose={() => setFormAbierto(false)}
        onCrear={(dto) => crear.mutate(dto)}
        isPending={crear.isPending}
      />

      <CancelarAsignacionModal
        isOpen={!!asignacionACancelar}
        asignacion={asignacionACancelar}
        onClose={() => setAsignacionACancelar(null)}
        onConfirmar={(motivo) => cancelar.mutate({ id: asignacionACancelar.id, motivo })}
        isPending={cancelar.isPending}
      />

      <AsignarDocenteModal
        isOpen={!!asignacionParaDocente}
        asignacion={asignacionParaDocente}
        onClose={() => setAsignacionParaDocente(null)}
        onConfirmar={(docenteAsesorId) =>
          asignarDocente.mutate({ asignacionId: asignacionParaDocente.id, docenteAsesorId })
        }
        isPending={asignarDocente.isPending}
      />
    </div>
  );
}
