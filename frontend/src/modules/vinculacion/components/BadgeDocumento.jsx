// src/modules/vinculacion/components/BadgeDocumento.jsx

/**
 * Badge de color para estados de documentos de vinculación.
 *
 * Estados posibles:
 *   PENDIENTE  → gris    (no subido aún)
 *   SUBIDO     → azul    (subido, pendiente de firmas)
 *   FIRMADO    → verde   (todas las firmas confirmadas)
 *
 * No modifica BadgeEstado.jsx del shared (que solo maneja booleano activo/inactivo).
 */

const COLORES = {
  PENDIENTE: { bg: '#f3f4f6', color: '#6b7280', texto: 'Pendiente' },
  SUBIDO:    { bg: '#dbeafe', color: '#1e40af', texto: 'Subido'    },
  FIRMADO:   { bg: '#d1fae5', color: '#065f46', texto: 'Firmado'   },
};

export default function BadgeDocumento({ estado }) {
  const { bg, color, texto } = COLORES[estado] || COLORES.PENDIENTE;

  return (
    <span
      style={{
        fontSize: 11,
        fontWeight: 600,
        padding: '3px 10px',
        borderRadius: 20,
        fontFamily: 'Arial, sans-serif',
        background: bg,
        color,
        whiteSpace: 'nowrap',
      }}
    >
      {texto}
    </span>
  );
}
