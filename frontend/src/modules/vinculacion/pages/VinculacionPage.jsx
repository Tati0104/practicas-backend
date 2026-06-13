// src/modules/vinculacion/pages/VinculacionPage.jsx

/**
 * Página: VinculacionPage
 * ────────────────────────
 * Página principal del módulo Vinculación y Documentos.
 *
 * ¿Qué hace esta página?
 *   - Muestra el listado paginado de procesos de vinculación activos.
 *   - En desktop (≥ 1280px): renderiza VinculacionTabla.
 *   - En móvil (< 1280px): renderiza tarjetas VinculacionCard apiladas.
 *   - Incluye la barra de filtros (VinculacionFiltros).
 *   - Maneja los estados: cargando → error → vacío → datos.
 *   - Muestra un toast.error si la carga falla.
 *   - El botón "Gestionar" de cada fila/card navega a VinculacionDetallePage.
 *
 * Hooks usados:
 *   - useVinculacion     → listado + filtros + paginación
 *   - usePermisos        → controla visibilidad de acciones (preparado para roles futuros)
 *
 * Responsive:
 *   - Usa window.matchMedia nativo para detectar el breakpoint (sin dependencias extra).
 *   - El listener se limpia al desmontar el componente (useEffect con cleanup).
 *
 * Rol que accede: COORD_PRACTICA, TUTOR_EMPRESARIAL, ESTUDIANTE
 */

import { useState, useEffect } from 'react';
import { useNavigate }         from 'react-router-dom';
import { toast }               from 'react-hot-toast';

import { useVinculacion }    from '../hooks/useVinculacion';
import { usePermisos }       from '../../../shared/hooks/usePermisos';
import VinculacionFiltros    from '../components/VinculacionFiltros';
import VinculacionTabla      from '../components/VinculacionTabla';
import VinculacionCard       from '../components/VinculacionCard';
import Paginacion            from '../../../shared/components/Paginacion';

// ── Hook interno para detección de breakpoint ─────────────────────────────────
// Evita instalar @react-hook/media-query u otras dependencias extra.
function useEsDesktop() {
  const [esDesktop, setEsDesktop] = useState(
    () => window.matchMedia('(min-width: 1280px)').matches
  );

  useEffect(() => {
    const mq = window.matchMedia('(min-width: 1280px)');
    const handler = (e) => setEsDesktop(e.matches);
    mq.addEventListener('change', handler);
    // Cleanup: elimina el listener al desmontar el componente
    return () => mq.removeEventListener('change', handler);
  }, []);

  return esDesktop;
}

// ── Componente principal ──────────────────────────────────────────────────────
export default function VinculacionPage() {
  const navigate   = useNavigate();
  const esDesktop  = useEsDesktop();

  // Datos, filtros y paginación del módulo
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

  // Permisos del usuario actual (stub devuelve todo true)
  const { canCreate } = usePermisos();

  // Si hay error al cargar, mostramos toast y no rompemos la UI
  useEffect(() => {
    if (isError) toast.error('Error al cargar los procesos de vinculación');
  }, [isError]);

  // ── Navegar al detalle de una práctica ──────────────────────────────────────
  const irADetalle = (vinculacion) => {
    navigate(`/vinculacion/${vinculacion.asignacionId}`);
  };

  // ── Render ──────────────────────────────────────────────────────────────────
  return (
    <div style={{ padding: 20, fontFamily: 'Arial, sans-serif' }}>

      {/* ── Encabezado de la página ── */}
      <div style={estilos.encabezado}>
        <div>
          <h1 style={estilos.titulo}>Vinculación y Documentos</h1>
          {/* Subtítulo con total de registros */}
          {!isLoading && (
            <p style={estilos.subtitulo}>
              {totalElementos} proceso{totalElementos !== 1 ? 's' : ''} encontrado{totalElementos !== 1 ? 's' : ''}
            </p>
          )}
        </div>
      </div>

      {/* ── Barra de filtros ── */}
      <VinculacionFiltros filtros={filtros} setFiltros={setFiltros} />

      {/* ── Estado: cargando ── */}
      {isLoading && (
        <div style={estilos.estadoCentrado}>
          <p style={{ color: '#6b7280' }}>Cargando procesos de vinculación...</p>
        </div>
      )}

      {/* ── Estado: error ── */}
      {isError && !isLoading && (
        <div style={estilos.errorBox}>
          <p>No se pudieron cargar los datos. Verifica tu conexión e intenta de nuevo.</p>
        </div>
      )}

      {/* ── Estado: sin datos ── */}
      {!isLoading && !isError && vinculaciones.length === 0 && (
        <div style={estilos.sinDatos}>
          <div style={{ fontSize: 36 }}>📁</div>
          <p>No hay procesos de vinculación que coincidan con los filtros aplicados.</p>
        </div>
      )}

      {/* ── Vista desktop: tabla ── */}
      {!isLoading && !isError && vinculaciones.length > 0 && esDesktop && (
        <VinculacionTabla
          vinculaciones={vinculaciones}
          isLoading={isLoading}
          onGestionar={irADetalle}
        />
      )}

      {/* ── Vista móvil: cards apiladas ── */}
      {!isLoading && !isError && vinculaciones.length > 0 && !esDesktop && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          {vinculaciones.map((v) => (
            <VinculacionCard
              key={v.practicaId}
              vinculacion={v}
              onGestionar={irADetalle}
            />
          ))}
        </div>
      )}

      {/* ── Paginación ── */}
      {!isLoading && (
        <Paginacion
          pagina={filtros.page}
          totalPaginas={totalPaginas}
          onCambiarPagina={irAPagina}
        />
      )}
    </div>
  );
}

// ── Estilos ───────────────────────────────────────────────────────────────────
const estilos = {
  encabezado: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 16,
  },
  titulo: {
    fontSize: 22,
    fontWeight: 800,
    color: '#111827',
    margin: 0,
  },
  subtitulo: {
    fontSize: 13,
    color: '#6b7280',
    margin: '4px 0 0 0',
  },
  estadoCentrado: {
    textAlign: 'center',
    padding: 40,
  },
  errorBox: {
    background: '#fee2e2',
    border: '1px solid #fca5a5',
    borderRadius: 8,
    padding: 16,
    color: '#991b1b',
    fontSize: 13,
  },
  sinDatos: {
    textAlign: 'center',
    padding: '40px 20px',
    color: '#9ca3af',
    fontSize: 14,
  },
};
