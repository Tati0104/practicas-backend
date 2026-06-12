// src/modules/vinculacion/components/PanelDocumento.jsx

/**
 * Componente: PanelDocumento
 * ──────────────────────────
 * Tarjeta individual que representa un documento (CARTA o CONVENIO) dentro del
 * módulo de vinculación.
 *
 * ¿Qué muestra?
 *   1. Nombre del tipo de documento ("Carta de Presentación" / "Convenio de Práctica").
 *   2. Badge de estado: PENDIENTE / SUBIDO / FIRMADO.
 *   3. Nombre del archivo (si ya fue subido).
 *   4. Botón "Descargar" (solo si hay archivo subido).
 *   5. Dropzone para subir el archivo (si aún no se ha subido o el rol lo permite).
 *   6. Indicador de progreso de firmas (ProgresoFirmas).
 *   7. Botón "Confirmar mi firma" por cada firmante pendiente (según rol actual).
 *
 * Props:
 *   documento        → object   — datos del documento { id, tipo, nombre, estado, firmas[] }
 *   asignacionId     → number   — ID de la asignación (para subir el archivo)
 *   onSubir          → function — (archivo) callback al subir el archivo
 *   onDescargar      → function — () callback al hacer clic en descargar
 *   onFirmar         → function — (tipoFirmante) callback al confirmar una firma
 *   isPendingSubir   → boolean  — true mientras se sube el archivo
 *   isPendingFirma   → boolean  — true mientras se confirma una firma
 *   puedeSubir       → boolean  — controla si se muestra el dropzone
 *   tipoFirmanteRol  → string   — rol del usuario actual ('COORDINADOR'|'TUTOR'|'ESTUDIANTE')
 */

import BadgeDocumento      from './BadgeDocumento';
import ProgresoFirmas      from './ProgresoFirmas';
import SubirDocumentoDropzone from './SubirDocumentoDropzone';

// Títulos legibles por tipo de documento
const TITULOS = {
  CARTA:    'Carta de Presentación',
  CONVENIO: 'Convenio de Práctica',
};

export default function PanelDocumento({
  documento,
  asignacionId,
  onSubir,
  onDescargar,
  onFirmar,
  isPendingSubir  = false,
  isPendingFirma  = false,
  puedeSubir      = false,
  tipoFirmanteRol = null,
}) {
  // El documento ya tiene archivo subido si su estado NO es PENDIENTE
  const tieneArchivo = documento.estado !== 'PENDIENTE';

  // El usuario actual puede firmar si:
  //   - su rol está en el array de firmas del documento
  //   - y esa firma aún no ha sido confirmada
  const firmaDelRol = tipoFirmanteRol
    ? documento.firmas?.find(
        (f) => f.tipoFirmante === tipoFirmanteRol && !f.firmado
      )
    : null;

  return (
    <div style={estilos.panel}>

      {/* ── Encabezado: título + badge ── */}
      <div style={estilos.encabezado}>
        <h3 style={estilos.titulo}>
          {TITULOS[documento.tipo] || documento.tipo}
        </h3>
        <BadgeDocumento estado={documento.estado} />
      </div>

      {/* ── Nombre del archivo (si existe) ── */}
      {tieneArchivo && documento.nombre && (
        <p style={estilos.nombreArchivo}>
          📎 {documento.nombre}
        </p>
      )}

      {/* ── Botón de descarga (solo si hay archivo) ── */}
      {tieneArchivo && (
        <button
          type="button"
          onClick={onDescargar}
          style={estilos.btnDescargar}
          aria-label={`Descargar ${TITULOS[documento.tipo]}`}
        >
          ⬇ Descargar
        </button>
      )}

      {/* ── Dropzone para subir (solo si el usuario tiene permiso y el doc está pendiente) ── */}
      {puedeSubir && !tieneArchivo && (
        <SubirDocumentoDropzone
          titulo={TITULOS[documento.tipo]}
          onSubir={(archivo) => onSubir(asignacionId, archivo)}
          isPending={isPendingSubir}
          deshabilitado={tieneArchivo}
        />
      )}

      {/* ── Separador ── */}
      <div style={estilos.separador} />

      {/* ── Progreso de firmas ── */}
      <ProgresoFirmas firmas={documento.firmas || []} />

      {/* ── Botón para que el firmante del rol actual confirme su firma ── */}
      {firmaDelRol && tieneArchivo && (
        <button
          type="button"
          onClick={() => onFirmar(tipoFirmanteRol)}
          disabled={isPendingFirma}
          style={{
            ...estilos.btnFirmar,
            opacity: isPendingFirma ? 0.7 : 1,
            cursor:  isPendingFirma ? 'wait' : 'pointer',
          }}
        >
          {isPendingFirma ? 'Procesando...' : '✍ Confirmar mi firma'}
        </button>
      )}
    </div>
  );
}

// ── Estilos ───────────────────────────────────────────────────────────────────
const estilos = {
  panel: {
    background: '#ffffff',
    border: '1px solid #e5e7eb',
    borderRadius: 10,
    padding: 18,
    display: 'flex',
    flexDirection: 'column',
    gap: 12,
    boxShadow: '0 1px 4px rgba(0,0,0,0.06)',
  },
  encabezado: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  titulo: {
    fontSize: 15,
    fontWeight: 700,
    color: '#111827',
    margin: 0,
  },
  nombreArchivo: {
    fontSize: 12,
    color: '#4b5563',
    margin: 0,
    wordBreak: 'break-all',
  },
  btnDescargar: {
    padding: '6px 14px',
    background: '#eff6ff',
    color: '#1d4ed8',
    border: '1px solid #bfdbfe',
    borderRadius: 6,
    fontSize: 12,
    fontWeight: 600,
    cursor: 'pointer',
    alignSelf: 'flex-start',
  },
  separador: {
    height: 1,
    background: '#f1f5f9',
    margin: '2px 0',
  },
  btnFirmar: {
    padding: '8px 16px',
    background: '#059669',
    color: '#fff',
    border: 'none',
    borderRadius: 6,
    fontSize: 13,
    fontWeight: 600,
    alignSelf: 'flex-start',
  },
};
