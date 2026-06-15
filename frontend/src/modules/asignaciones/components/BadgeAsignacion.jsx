// src/modules/asignaciones/components/BadgeAsignacion.jsx

/**
 * Badge de color para los estados de una asignación.
 * Estados posibles: ASIGNADA | EN_PROCESO_VINCULACION | VINCULADA | CANCELADA
 *
 * Nota: el BadgeEstado compartido solo maneja activo/inactivo (booleano),
 * por eso creamos uno específico para los estados de asignación.
 */

// Mapa de estado → { fondo, texto }
const COLORES = {
  ASIGNADA:       { bg: '#dbeafe', color: '#1e40af' }, // azul
  EN_PROCESO_VINCULACION: { bg: '#fef9c3', color: '#854d0e' }, // amarillo
  VINCULADA:      { bg: '#d1fae5', color: '#065f46' }, // verde
  CANCELADA:      { bg: '#fee2e2', color: '#991b1b' }, // rojo
};

const ETIQUETAS = {
  ASIGNADA:       'Asignada',
  EN_PROCESO_VINCULACION: 'En Vinculación',
  VINCULADA:      'Vinculada',
  CANCELADA:      'Cancelada',
};

export default function BadgeAsignacion({ estado }) {
  const { bg, color } = COLORES[estado] || { bg: '#f3f4f6', color: '#374151' };

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
      {ETIQUETAS[estado] || estado}
    </span>
  );
}
