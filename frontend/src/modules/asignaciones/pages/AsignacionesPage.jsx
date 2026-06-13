// src/modules/asignaciones/pages/AsignacionesPage.jsx

/**
 * Página principal del módulo Asignaciones.
 *
 * Responsabilidades:
 *   - Mostrar indicadores estadísticos (IndicadoresAsignaciones).
 *   - Mostrar la barra de filtros (AsignacionesFiltros).
 *   - En desktop (≥1280px): tabla (AsignacionesTabla).
 *   - En mobile: cards apiladas (AsignacionCard).
 *   - Botón "Nueva asignación" que abre AsignacionForm.
 *   - Modal de cancelación (CancelarAsignacionModal).
 *   - Paginación simple.
 *
 * Hooks usados:
 *   - useAsignaciones  → datos + filtros
 *   - useAsignacionesMutaciones → crear, cancelar
 *   - usePermisos → controla qué botones se muestran
 */
import { useState, useEffect } from 'react';
import { toast } from 'react-hot-toast';
import { useAsignaciones }         from '../hooks/useAsignaciones';
import { useAsignacionesMutaciones } from '../hooks/useAsignacionesMutaciones';
import { usePermisos }              from '../../../shared/hooks/usePermisos';

import AsignacionesFiltros       from '../components/AsignacionesFiltros';
import AsignacionesTabla         from '../components/AsignacionesTabla';
import AsignacionCard            from '../components/AsignacionCard';
import AsignacionForm            from '../components/AsignacionForm';
import CancelarAsignacionModal   from '../components/CancelarAsignacionModal';
import IndicadoresAsignaciones   from '../components/IndicadoresAsignaciones';
import Paginacion                from '../../../shared/components/Paginacion';

// Hook nativo para responsive (evita depencia extra)
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

export default function AsignacionesPage() {
  const esDesktop = useEsDesktop();

  // Estado del modal de creación
  const [formAbierto, setFormAbierto] = useState(false);
  // Asignación seleccionada para cancelar
  const [asignacionACancelar, setAsignacionACancelar] = useState(null);

  const { asignaciones, totalPaginas, isLoading, isError, filtros, setFiltros, irAPagina } =
    useAsignaciones();

  const { canCreate } = usePermisos();

  const { crear, cancelar } = useAsignacionesMutaciones({
    onSuccess: () => {
      setFormAbierto(false);
      setAsignacionACancelar(null);
    },
  });

  // Si hay error al cargar, mostramos toast
  useEffect(() => {
    if (isError) toast.error('Error al cargar asignaciones');
  }, [isError]);

  return (
    <div style={{ padding: 20, fontFamily: 'Arial, sans-serif' }}>
      {/* Encabezado */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <h1 style={{ fontSize: 22, fontWeight: 800, color: '#111827', margin: 0 }}>
          Asignaciones
        </h1>
        {canCreate && (
          <button
            onClick={() => setFormAbierto(true)}
            style={estilos.btnNuevo}
          >
            + Nueva asignación
          </button>
        )}
      </div>

      {/* Tarjetas de indicadores */}
      <IndicadoresAsignaciones asignaciones={asignaciones} />

      {/* Filtros */}
      <AsignacionesFiltros filtros={filtros} setFiltros={setFiltros} />

      {/* Spinner de carga */}
      {isLoading && (
        <div style={{ textAlign: 'center', padding: 40, color: '#6b7280' }}>
          Cargando asignaciones...
        </div>
      )}

      {/* Estado vacío */}
      {!isLoading && asignaciones.length === 0 && (
        <div style={estilos.sinDatos}>
          <div style={{ fontSize: 36 }}>📋</div>
          <p>No hay asignaciones que coincidan con los filtros.</p>
        </div>
      )}

      {/* Vista desktop: tabla */}
      {!isLoading && asignaciones.length > 0 && esDesktop && (
        <AsignacionesTabla
          asignaciones={asignaciones}
          isLoading={isLoading}
          canCancelar={canCreate}
          onCancelar={(a) => setAsignacionACancelar(a)}
        />
      )}

      {/* Vista mobile: cards */}
      {!isLoading && asignaciones.length > 0 && !esDesktop && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          {asignaciones.map((a) => (
            <AsignacionCard
              key={a.id}
              asignacion={a}
              canCancelar={canCreate}
              onCancelar={(a) => setAsignacionACancelar(a)}
            />
          ))}
        </div>
      )}

      {/* Paginación */}
      <Paginacion
        pagina={filtros.page}
        totalPaginas={totalPaginas}
        onCambiarPagina={irAPagina}
      />

      {/* Modal: nueva asignación */}
      <AsignacionForm
        isOpen={formAbierto}
        onClose={() => setFormAbierto(false)}
        onCrear={(dto) => crear.mutate(dto)}
        isPending={crear.isPending}
      />

      {/* Modal: cancelar asignación */}
      <CancelarAsignacionModal
        isOpen={!!asignacionACancelar}
        asignacion={asignacionACancelar}
        onClose={() => setAsignacionACancelar(null)}
        onConfirmar={(motivo) =>
          cancelar.mutate({ id: asignacionACancelar.id, motivo })
        }
        isPending={cancelar.isPending}
      />
    </div>
  );
}

const estilos = {
  btnNuevo: {
    padding: '9px 18px',
    background: '#2563eb',
    color: '#fff',
    border: 'none',
    borderRadius: 8,
    fontSize: 14,
    fontWeight: 700,
    cursor: 'pointer',
  },
  sinDatos: {
    textAlign: 'center',
    padding: '40px 20px',
    color: '#9ca3af',
    fontSize: 14,
  },
};
