// src/modules/vinculacion/components/ConfirmarFirmaModal.jsx

/**
 * Componente: ConfirmarFirmaModal
 * ────────────────────────────────
 * Modal de confirmación para que un firmante confirme su firma sobre un convenio.
 *
 * ¿Qué hace?
 *   - Muestra el nombre del documento y el tipo de firmante.
 *   - Pide al usuario confirmar explícitamente antes de llamar al backend.
 *   - Al confirmar llama a onConfirmar() que dispara la mutación confirmarFirma.
 *   - Mientras isPending, desactiva los botones para evitar doble envío.
 *   - Al cerrar (Cancelar o tras éxito) llama a onClose().
 *
 * Props:
 *   isOpen         → boolean   — controla visibilidad del modal
 *   documento      → object    — { id, tipo, nombre } del documento a firmar
 *   tipoFirmante   → string    — 'COORDINADOR' | 'TUTOR' | 'ESTUDIANTE'
 *   onClose        → function  — cierra el modal sin hacer nada
 *   onConfirmar    → function  — se llama cuando el usuario confirma la firma
 *   isPending      → boolean   — true mientras la petición PATCH está en curso
 */

// Etiquetas legibles por tipo de firmante
const ETIQUETAS_FIRMANTE = {
  COORDINADOR: 'Coordinador de Prácticas',
  TUTOR:       'Tutor Empresarial',
  ESTUDIANTE:  'Estudiante',
};

export default function ConfirmarFirmaModal({
  isOpen,
  documento,
  tipoFirmante,
  onClose,
  onConfirmar,
  isPending = false,
}) {
  // Si el modal no está abierto o no hay documento, no renderizamos nada
  if (!isOpen || !documento) return null;

  return (
    // ── Overlay semitransparente que bloquea el fondo ──
    <div
      role="dialog"
      aria-labelledby="titulo-confirmar-firma"
      aria-modal="true"
      style={estilos.overlay}
    >
      {/* ── Caja del modal ── */}
      <div style={estilos.modal}>

        {/* Título del modal */}
        <h2 id="titulo-confirmar-firma" style={estilos.titulo}>
          Confirmar firma
        </h2>

        {/* Resumen: qué documento y quién firma */}
        <div style={estilos.resumen}>
          <p style={estilos.resumenFila}>
            <strong>Documento:</strong> {documento.tipo === 'CARTA' ? 'Carta de Presentación' : 'Convenio de Práctica'}
          </p>
          <p style={estilos.resumenFila}>
            <strong>Archivo:</strong> {documento.nombre || '(sin nombre)'}
          </p>
          <p style={estilos.resumenFila}>
            <strong>Firmante:</strong> {ETIQUETAS_FIRMANTE[tipoFirmante] || tipoFirmante}
          </p>
        </div>

        {/* Advertencia */}
        <p style={estilos.advertencia}>
          ⚠️ Esta acción es irreversible. Una vez confirmada la firma no podrá deshacerse.
        </p>

        {/* Botones de acción */}
        <div style={estilos.botones}>
          {/* Cancelar: cierra el modal sin hacer nada */}
          <button
            type="button"
            onClick={onClose}
            disabled={isPending}
            style={estilos.btnCancelar}
          >
            Cancelar
          </button>

          {/* Confirmar: dispara la mutación */}
          <button
            type="button"
            onClick={onConfirmar}
            disabled={isPending}
            style={{
              ...estilos.btnConfirmar,
              opacity: isPending ? 0.7 : 1,
              cursor:  isPending ? 'wait' : 'pointer',
            }}
          >
            {isPending ? 'Procesando...' : 'Confirmar firma'}
          </button>
        </div>
      </div>
    </div>
  );
}

// ── Estilos en línea ─────────────────────────────────────────────────────────
const estilos = {
  overlay: {
    position: 'fixed',
    inset: 0,
    background: 'rgba(0,0,0,0.45)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 50,
    padding: 16,
  },
  modal: {
    background: '#fff',
    borderRadius: 10,
    width: '100%',
    maxWidth: 440,
    padding: 24,
    boxShadow: '0 10px 25px rgba(0,0,0,0.15)',
  },
  titulo: {
    fontSize: 18,
    fontWeight: 700,
    color: '#111827',
    marginBottom: 14,
  },
  resumen: {
    background: '#f8fafc',
    border: '1px solid #e5e7eb',
    borderRadius: 8,
    padding: '10px 14px',
    marginBottom: 12,
  },
  resumenFila: {
    fontSize: 13,
    color: '#374151',
    margin: '4px 0',
  },
  advertencia: {
    fontSize: 12,
    color: '#92400e',
    background: '#fef9c3',
    border: '1px solid #fde68a',
    borderRadius: 6,
    padding: '8px 12px',
    marginBottom: 16,
  },
  botones: {
    display: 'flex',
    justifyContent: 'flex-end',
    gap: 8,
  },
  btnCancelar: {
    padding: '8px 16px',
    background: '#f1f5f9',
    color: '#374151',
    border: '1px solid #d1d5db',
    borderRadius: 6,
    fontSize: 13,
    cursor: 'pointer',
  },
  btnConfirmar: {
    padding: '8px 18px',
    background: '#059669',
    color: '#fff',
    border: 'none',
    borderRadius: 6,
    fontSize: 13,
    fontWeight: 600,
  },
};
