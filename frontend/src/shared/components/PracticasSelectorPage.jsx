import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import { useSeguimiento } from '@/modules/seguimiento/hooks/useSeguimiento';
import SeguimientoFiltros from '@/modules/seguimiento/components/SeguimientoFiltros';
import SeguimientoTabla from '@/modules/seguimiento/components/SeguimientoTabla';
import PracticaCard from '@/modules/seguimiento/components/PracticaCard';
import Paginacion from '@/shared/components/Paginacion';
import PageHeader from '@/shared/components/ui/PageHeader';

function useEsDesktop() {
  const [esDesktop, setEsDesktop] = useState(() =>
    window.matchMedia('(min-width: 1024px)').matches
  );

  useEffect(() => {
    const mq = window.matchMedia('(min-width: 1024px)');
    const handler = (e) => setEsDesktop(e.matches);
    mq.addEventListener('change', handler);
    return () => mq.removeEventListener('change', handler);
  }, []);

  return esDesktop;
}

/**
 * Página genérica para seleccionar una práctica antes de entrar a un sub-módulo.
 * Reutiliza el tablero de seguimiento (DRY) con distinta ruta destino.
 */
export default function PracticasSelectorPage({
  titulo,
  descripcion,
  accionLabel = 'Abrir',
  construirRuta,
}) {
  const navigate = useNavigate();
  const esDesktop = useEsDesktop();
  const { practicas, totalPaginas, isLoading, isError, filtros, setFiltros, irAPagina } =
    useSeguimiento();

  useEffect(() => {
    if (isError) toast.error('Error al cargar las prácticas');
  }, [isError]);

  const irAModulo = (id) => navigate(construirRuta(id));

  return (
    <div>
      <PageHeader titulo={titulo} descripcion={descripcion} />

      <SeguimientoFiltros filtros={filtros} setFiltros={setFiltros} />

      {isLoading && (
        <p className="py-10 text-center text-sm text-gray-500">Cargando prácticas...</p>
      )}

      {!isLoading && practicas.length === 0 && (
        <div className="rounded-lg border border-dashed border-gray-300 py-12 text-center text-gray-400">
          No hay prácticas que coincidan con los filtros.
        </div>
      )}

      {!isLoading && practicas.length > 0 && esDesktop && (
        <SeguimientoTabla
          practicas={practicas}
          onVerDetalle={irAModulo}
          etiquetaAccion={accionLabel}
        />
      )}

      {!isLoading && practicas.length > 0 && !esDesktop && (
        <div className="flex flex-col gap-3">
          {practicas.map((p) => (
            <PracticaCard
              key={p.id}
              practica={p}
              onVerDetalle={irAModulo}
              etiquetaAccion={accionLabel}
            />
          ))}
        </div>
      )}

      <Paginacion
        pagina={filtros.page}
        totalPaginas={totalPaginas}
        onCambiarPagina={irAPagina}
      />
    </div>
  );
}
