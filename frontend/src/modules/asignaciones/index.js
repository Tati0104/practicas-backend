// src/modules/asignaciones/index.js

// Exportaciones centrales del módulo Asignaciones
// Permite importar desde '@/modules/asignaciones' en lugar de rutas largas

// Páginas
export { default as AsignacionesPage }      from './pages/AsignacionesPage';
export { default as AsignacionDetallePage } from './pages/AsignacionDetallePage';

// Componentes
export { default as AsignacionesTabla }       from './components/AsignacionesTabla';
export { default as AsignacionesFiltros }     from './components/AsignacionesFiltros';
export { default as AsignacionForm }          from './components/AsignacionForm';
export { default as CancelarAsignacionModal } from './components/CancelarAsignacionModal';
export { default as HistorialEstados }        from './components/HistorialEstados';
export { default as IndicadoresAsignaciones } from './components/IndicadoresAsignaciones';
export { default as AsignacionCard }          from './components/AsignacionCard';
export { default as BadgeAsignacion }         from './components/BadgeAsignacion';

// Hooks
export { useAsignaciones }           from './hooks/useAsignaciones';
export { useAsignacionesMutaciones } from './hooks/useAsignacionesMutaciones';
export { useAsignacionDetalle }      from './hooks/useAsignacionDetalle';
export { useEstudiantesAptos }       from './hooks/useEstudiantesAptos';
export { useVacantesActivas }        from './hooks/useVacantesActivas';
