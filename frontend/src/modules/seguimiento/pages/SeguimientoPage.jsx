// src/modules/seguimiento/pages/SeguimientoPage.jsx

import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import useAuthStore from '@/store/authStore';
import { useSeguimiento } from '../hooks/useSeguimiento';
import IndicadoresSeguimiento from '../components/IndicadoresSeguimiento';
import SeguimientoFiltros from '../components/SeguimientoFiltros';
import SeguimientoFiltrosEstudiante from '../components/SeguimientoFiltrosEstudiante';
import SeguimientoTabla from '../components/SeguimientoTabla';
import PracticaCard from '../components/PracticaCard';
import AlertasPanel from '../components/AlertasPanel';
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

export default function SeguimientoPage() {
  const navigate = useNavigate();
  const esDesktop = useEsDesktop();
  const esEstudiante = useAuthStore((state) => state.rol) === 'ESTUDIANTE';
  const { practicas, practicasTodas, totalPaginas, isLoading, isError, filtros, setFiltros, irAPagina } =
    useSeguimiento();

  useEffect(() => {
    if (isError) toast.error('Error al cargar el tablero de seguimiento');
  }, [isError]);

  const verDetalle = (id) => navigate(`/seguimiento/${id}`);

  return (
    <div>
      <PageHeader
        titulo={esEstudiante ? 'Mis seguimientos' : 'Seguimiento'}
        descripcion={
          esEstudiante
            ? 'Entregas de bitácora, comentarios del docente y estado de revisión'
            : 'Tablero de seguimiento de prácticas'
        }
      />

      {!esEstudiante && <IndicadoresSeguimiento practicas={practicas} />}

      <div className="mt-5 flex flex-col gap-5 xl:flex-row xl:items-start">
        <div className="min-w-0 flex-1">
          {esEstudiante ? (
            <SeguimientoFiltrosEstudiante
              filtros={filtros}
              setFiltros={setFiltros}
              practicas={practicasTodas}
            />
          ) : (
            <SeguimientoFiltros filtros={filtros} setFiltros={setFiltros} />
          )}

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
              onVerDetalle={verDetalle}
              ocultarEstudiante={esEstudiante}
            />
          )}

          {!isLoading && practicas.length > 0 && !esDesktop && (
            <div className="flex flex-col gap-3">
              {practicas.map((p) => (
                <PracticaCard
                  key={p.id}
                  practica={p}
                  onVerDetalle={verDetalle}
                  modoEstudiante={esEstudiante}
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

        {!esEstudiante && esDesktop && (
          <div className="w-full shrink-0 xl:w-72">
            <AlertasPanel />
          </div>
        )}
      </div>
    </div>
  );
}
