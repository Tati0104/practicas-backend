// src/modules/seguimiento/pages/SeguimientoPage.jsx

/**
 * Página principal del módulo Seguimiento.
 * Muestra indicadores, filtros, tabla/cards de prácticas y panel de alertas.
 */
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import { useSeguimiento }           from '../hooks/useSeguimiento';
import { usePermisos }              from '../../../shared/hooks/usePermisos';
import IndicadoresSeguimiento       from '../components/IndicadoresSeguimiento';
import SeguimientoFiltros           from '../components/SeguimientoFiltros';
import SeguimientoTabla             from '../components/SeguimientoTabla';
import PracticaCard                 from '../components/PracticaCard';
import AlertasPanel                 from '../components/AlertasPanel';

function useEsDesktop() {
  const [esDesktop, setEsDesktop] = useState(() => window.matchMedia('(min-width: 1280px)').matches);
  useEffect(() => {
    const mq = window.matchMedia('(min-width: 1280px)');
    const handler = (e) => setEsDesktop(e.matches);
    mq.addEventListener('change', handler);
    return () => mq.removeEventListener('change', handler);
  }, []);
  return esDesktop;
}

export default function SeguimientoPage() {
  const navigate   = useNavigate();
  const esDesktop  = useEsDesktop();
  const { practicas, totalPaginas, isLoading, isError, filtros, setFiltros, irAPagina } = useSeguimiento();

  useEffect(() => {
    if (isError) toast.error('Error al cargar el tablero de seguimiento');
  }, [isError]);

  const verDetalle = (id) => navigate(`/seguimiento/${id}`);

  return (
    <div style={{ padding: 20, fontFamily: 'Arial, sans-serif' }}>
      <h1 style={{ fontSize: 22, fontWeight: 800, color: '#111827', marginBottom: 4 }}>Seguimiento</h1>
      <p style={{ color: '#6b7280', fontSize: 14, marginBottom: 20, marginTop: 0 }}>
        Tablero de seguimiento de prácticas
      </p>

      <IndicadoresSeguimiento practicas={practicas} />

      <div style={{ display: 'flex', gap: 20, alignItems: 'flex-start' }}>
        {/* Columna principal */}
        <div style={{ flex: 1, minWidth: 0 }}>
          <SeguimientoFiltros filtros={filtros} setFiltros={setFiltros} />

          {isLoading && (
            <div style={{ textAlign: 'center', padding: 40, color: '#6b7280' }}>Cargando prácticas...</div>
          )}

          {!isLoading && practicas.length === 0 && (
            <div style={{ textAlign: 'center', padding: '40px 20px', color: '#9ca3af' }}>
              <div style={{ fontSize: 36 }}>📋</div>
              <p>No hay prácticas que coincidan con los filtros.</p>
            </div>
          )}

          {!isLoading && practicas.length > 0 && esDesktop && (
            <SeguimientoTabla practicas={practicas} onVerDetalle={verDetalle} />
          )}

          {!isLoading && practicas.length > 0 && !esDesktop && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
              {practicas.map((p) => (
                <PracticaCard key={p.id} practica={p} onVerDetalle={verDetalle} />
              ))}
            </div>
          )}

          {totalPaginas > 1 && (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: 16, marginTop: 20 }}>
              <button onClick={() => irAPagina(filtros.page - 1)} disabled={filtros.page === 0} style={btnPag}>← Anterior</button>
              <span style={{ fontSize: 13, color: '#374151' }}>Página {filtros.page + 1} de {totalPaginas}</span>
              <button onClick={() => irAPagina(filtros.page + 1)} disabled={filtros.page >= totalPaginas - 1} style={btnPag}>Siguiente →</button>
            </div>
          )}
        </div>

        {/* Panel de alertas (solo desktop) */}
        {esDesktop && (
          <div style={{ width: 280, flexShrink: 0 }}>
            <AlertasPanel />
          </div>
        )}
      </div>
    </div>
  );
}

const btnPag = { padding: '6px 14px', border: '1px solid #d1d5db', borderRadius: 6, background: '#fff', fontSize: 13, cursor: 'pointer' };
