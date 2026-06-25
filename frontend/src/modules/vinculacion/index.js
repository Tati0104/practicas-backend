// src/modules/vinculacion/index.js

/**
 * Punto de entrada del módulo Vinculación y Documentos.
 *
 * Centraliza todas las exportaciones del módulo para que el router
 * y otros módulos puedan importar desde una sola ruta:
 *
 *   import { VinculacionPage } from '../modules/vinculacion'
 *
 * En lugar de:
 *   import VinculacionPage from '../modules/vinculacion/pages/VinculacionPage'
 *
 * Esto sigue la regla 8 del documento de arquitectura:
 *   "Exportar siempre desde index.js del módulo."
 */

// ── Páginas ───────────────────────────────────────────────────────────────────
export { default as VinculacionPage }       from './pages/VinculacionPage';
export { default as VinculacionDetallePage } from './pages/VinculacionDetallePage';

// ── Componentes ───────────────────────────────────────────────────────────────
export { default as VinculacionTabla }        from './components/VinculacionTabla';
export { default as VinculacionFiltros }      from './components/VinculacionFiltros';
export { default as VinculacionCard }         from './components/VinculacionCard';
export { default as PanelDocumento }          from './components/PanelDocumento';
export { default as SubirDocumentoDropzone }  from './components/SubirDocumentoDropzone';
export { default as ProgresoFirmas }          from './components/ProgresoFirmas';
export { default as ConfirmarFirmaModal }     from './components/ConfirmarFirmaModal';
export { default as BadgeDocumento }          from './components/BadgeDocumento';

// ── Hooks ─────────────────────────────────────────────────────────────────────
export { useVinculacion }           from './hooks/useVinculacion';
export { useVinculacionDocumentos } from './hooks/useVinculacionDocumentos';
export { useVinculacionMutaciones } from './hooks/useVinculacionMutaciones';
export { useVinculacionDetalle }    from './hooks/useVinculacionDetalle';
