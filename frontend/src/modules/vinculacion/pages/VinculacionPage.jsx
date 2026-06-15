// src/modules/vinculacion/pages/VinculacionPage.jsx

import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { FolderOpen } from 'lucide-react';
import { toast } from 'react-hot-toast';
import { useVinculacion } from '../hooks/useVinculacion';
import VinculacionFiltros from '../components/VinculacionFiltros';
import VinculacionTabla from '../components/VinculacionTabla';
import VinculacionCard from '../components/VinculacionCard';
import Paginacion from '../../../shared/components/Paginacion';
import { PageHeader } from '@/shared/components/ui';

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

export default function VinculacionPage() {
  const navigate = useNavigate();
  const esDesktop = useEsDesktop();
  const {
    vinculaciones,
    totalPaginas,
    totalElementos,
    isLoading,
    isError,
    filtros,
    setFiltros,
    irAPagina,
  } = useVinculacion();

  useEffect(() => {
    if (isError) toast.error('Error al cargar los procesos de vinculación');
  }, [isError]);

  const irADetalle = (vinculacion) => {
    navigate(`/vinculacion/${vinculacion.asignacionId}`);
  };

  const subtitulo =
    !isLoading &&
    `${totalElementos} proceso${totalElementos !== 1 ? 's' : ''} encontrado${totalElementos !== 1 ? 's' : ''}`;

  return (
    <div>
      <PageHeader
        titulo="Vinculación y documentos"
        descripcion={
          subtitulo ||
          'Gestión de hoja de vida, carta, proyecto y convenio de práctica'
        }
      />

      <VinculacionFiltros filtros={filtros} setFiltros={setFiltros} />

      {isLoading && (
        <p className="py-10 text-center text-sm text-gray-500">Cargando procesos de vinculación...</p>
      )}

      {isError && !isLoading && (
        <div className="rounded-lg border border-red-200 bg-red-50 p-4 text-sm text-red-800">
          No se pudieron cargar los datos. Verifica tu conexión e intenta de nuevo.
        </div>
      )}

      {!isLoading && !isError && vinculaciones.length === 0 && (
        <div className="rounded-lg border border-dashed border-gray-300 py-12 text-center text-gray-400">
          <FolderOpen className="mx-auto mb-2 h-8 w-8 opacity-50" aria-hidden="true" />
          No hay procesos de vinculación que coincidan con los filtros aplicados.
        </div>
      )}

      {!isLoading && !isError && vinculaciones.length > 0 && esDesktop && (
        <VinculacionTabla
          vinculaciones={vinculaciones}
          isLoading={isLoading}
          onGestionar={irADetalle}
        />
      )}

      {!isLoading && !isError && vinculaciones.length > 0 && !esDesktop && (
        <div className="flex flex-col gap-3">
          {vinculaciones.map((v) => (
            <VinculacionCard key={v.asignacionId ?? v.practicaId} vinculacion={v} onGestionar={irADetalle} />
          ))}
        </div>
      )}

      {!isLoading && (
        <Paginacion pagina={filtros.page} totalPaginas={totalPaginas} onCambiarPagina={irAPagina} />
      )}
    </div>
  );
}
