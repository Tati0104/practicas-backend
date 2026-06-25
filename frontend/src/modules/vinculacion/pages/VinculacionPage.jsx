// src/modules/vinculacion/pages/VinculacionPage.jsx

import { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { FolderOpen } from 'lucide-react';
import { toast } from 'react-hot-toast';
import { useVinculacion } from '../hooks/useVinculacion';
import VinculacionFiltros from '../components/VinculacionFiltros';
import VinculacionFiltrosEstudiante from '../components/VinculacionFiltrosEstudiante';
import VinculacionTabla from '../components/VinculacionTabla';
import VinculacionCard from '../components/VinculacionCard';
import Paginacion from '../../../shared/components/Paginacion';
import { PageHeader } from '@/shared/components/ui';
import useAuthStore from '@/store/authStore';

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
  const esTutor = useAuthStore((state) => state.rol) === 'TUTOR_EMPRESARIAL';
  const esEstudiante = useAuthStore((state) => state.rol) === 'ESTUDIANTE';
  const {
    vinculaciones: vinculacionesRaw,
    totalPaginas,
    totalElementos,
    isLoading,
    isError,
    filtros,
    setFiltros,
    irAPagina,
  } = useVinculacion();

  const vinculaciones = useMemo(() => {
    let lista = vinculacionesRaw;
    if (filtros.numeroPractica) {
      lista = lista.filter(
        (v) => String(v.numeroPractica) === String(filtros.numeroPractica)
      );
    }
    if (filtros.estadoPractica) {
      lista = lista.filter((v) => v.estadoPractica === filtros.estadoPractica);
    }
    return lista;
  }, [vinculacionesRaw, filtros.numeroPractica, filtros.estadoPractica]);

  useEffect(() => {
    if (isError) toast.error('Error al cargar los procesos de vinculación');
  }, [isError]);

  const irADetalle = (vinculacion) => {
    if (vinculacion.asignacionId) {
      navigate(`/vinculacion/${vinculacion.asignacionId}`);
    } else if (vinculacion.practicaId) {
      navigate(`/vinculacion/practica/${vinculacion.practicaId}`);
    }
  };

  const subtitulo =
    !isLoading &&
    `${totalElementos} proceso${totalElementos !== 1 ? 's' : ''} encontrado${totalElementos !== 1 ? 's' : ''}`;

  return (
    <div>
      <PageHeader
        titulo={
          esEstudiante
            ? 'Mis documentos de práctica'
            : esTutor
              ? 'Documentos de mis practicantes'
              : 'Vinculación y documentos'
        }
        descripcion={
          esEstudiante
            ? 'Sube documentos, consulta el estado de vinculación y firma el convenio'
            : subtitulo ||
              (esTutor
                ? 'Revisa los documentos y firma el convenio de tus estudiantes asignados'
                : 'Gestión de hoja de vida, carta, proyecto y convenio de práctica')
        }
      />

      {esEstudiante ? (
        <VinculacionFiltrosEstudiante
          filtros={filtros}
          setFiltros={setFiltros}
          practicasDisponibles={vinculacionesRaw}
        />
      ) : (
        <VinculacionFiltros filtros={filtros} setFiltros={setFiltros} />
      )}

      {!isLoading && !isError && vinculaciones.length > 0 && (
        <div className="mb-4 rounded-lg border border-blue-100 bg-blue-50 px-4 py-3 text-sm text-blue-900">
          {esEstudiante ? (
            <>
              Selecciona una práctica y haz clic en <strong>Gestionar</strong> para subir documentos
              o firmar el convenio. Si la práctica ya finalizó, podrás consultar el historial en
              modo lectura.
            </>
          ) : esTutor ? (
            <>
              Para revisar documentos y firmar el convenio, haz clic en{' '}
              <strong>Gestionar</strong> en el estudiante correspondiente.
            </>
          ) : (
            <>
              Para subir hoja de vida, carta, proyecto o convenio, haz clic en{' '}
              <strong>Gestionar</strong> en el estudiante correspondiente.
            </>
          )}
        </div>
      )}

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
          <p>No hay procesos de vinculación que coincidan con los filtros aplicados.</p>
          <p className="mt-2 text-xs text-gray-500">
            {esEstudiante
              ? 'Cuando el coordinador académico te habilite una práctica (Práctica 1, 2, etc.), aparecerá aquí para subir documentos y firmar el convenio.'
              : esTutor
                ? 'Cuando te asignen practicantes, aparecerán aquí para que puedas firmar el convenio.'
                : 'Si aún no hay asignaciones, créalas en Vacantes y Postulaciones.'}
          </p>
        </div>
      )}

      {!isLoading && !isError && vinculaciones.length > 0 && esDesktop && (
        <VinculacionTabla
          vinculaciones={vinculaciones}
          isLoading={isLoading}
          onGestionar={irADetalle}
          ocultarEstudiante={esEstudiante}
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
