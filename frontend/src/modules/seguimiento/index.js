// src/modules/seguimiento/index.js

// Exportaciones centrales del módulo Seguimiento

// Páginas
export { default as SeguimientoPage }     from './pages/SeguimientoPage';
export { default as PracticaDetallePage } from './pages/PracticaDetallePage';

// Componentes
export { default as SeguimientoTabla }       from './components/SeguimientoTabla';
export { default as SeguimientoFiltros }     from './components/SeguimientoFiltros';
export { default as PracticaCard }           from './components/PracticaCard';
export { default as IndicadoresSeguimiento } from './components/IndicadoresSeguimiento';
export { default as AlertasPanel }           from './components/AlertasPanel';
export { default as TimelineSeguimiento }    from './components/TimelineSeguimiento';
export { default as ObservacionModal }       from './components/ObservacionModal';
export { default as AvanceTutorModal }       from './components/AvanceTutorModal';
export { default as BitacoraModal }          from './components/BitacoraModal';

// Hooks
export { useSeguimiento }            from './hooks/useSeguimiento';
export { useSeguimientoMutaciones }  from './hooks/useSeguimientoMutaciones';
export { useAlertas }                from './hooks/useAlertas';
export { usePracticaSeguimiento }    from './hooks/usePracticaSeguimiento';
