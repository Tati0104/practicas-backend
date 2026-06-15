// src/modules/asignaciones/components/IndicadoresAsignaciones.jsx

/**
 * Tarjetas de resumen estadístico del módulo Asignaciones.
 *
 * Muestra 3 indicadores:
 *   1. Total asignaciones activas (ASIGNADA + EN_VINCULACION)
 *   2. En proceso de vinculación (EN_VINCULACION)
 *   3. Canceladas este mes (CANCELADA)
 *
 * Calcula los valores a partir de la lista recibida por props (sin petición extra).
 * En desktop muestra 3 columnas, en mobile 1 columna.
 */
export default function IndicadoresAsignaciones({ asignaciones = [] }) {
  // Calcula cuántas tienen cada estado
  const activas = asignaciones.filter(
    (a) => a.estado === 'ASIGNADA' || a.estado === 'EN_PROCESO_VINCULACION'
  ).length;

  const enVinculacion = asignaciones.filter(
    (a) => a.estado === 'EN_PROCESO_VINCULACION'
  ).length;

  // "Canceladas este mes": las canceladas cuya fecha de asignación sea del mes actual
  const ahora = new Date();
  const canceladasMes = asignaciones.filter((a) => {
    if (a.estado !== 'CANCELADA' || !a.fechaAsignacion) return false;
    const fecha = new Date(a.fechaAsignacion);
    return (
      fecha.getMonth() === ahora.getMonth() &&
      fecha.getFullYear() === ahora.getFullYear()
    );
  }).length;

  const tarjetas = [
    {
      titulo: 'Asignaciones activas',
      valor: activas,
      color: '#2563eb',
      fondo: '#eff6ff',
      icono: '📋',
    },
    {
      titulo: 'En vinculación',
      valor: enVinculacion,
      color: '#854d0e',
      fondo: '#fef9c3',
      icono: '🔗',
    },
    {
      titulo: 'Canceladas este mes',
      valor: canceladasMes,
      color: '#991b1b',
      fondo: '#fee2e2',
      icono: '🚫',
    },
  ];

  return (
    <div
      style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
        gap: 12,
        marginBottom: 20,
      }}
    >
      {tarjetas.map((t) => (
        <div
          key={t.titulo}
          style={{
            background: t.fondo,
            border: `1px solid ${t.color}22`,
            borderRadius: 10,
            padding: '14px 18px',
            display: 'flex',
            flexDirection: 'column',
            gap: 4,
          }}
        >
          <div style={{ fontSize: 20 }}>{t.icono}</div>
          <div style={{ fontSize: 28, fontWeight: 800, color: t.color }}>{t.valor}</div>
          <div style={{ fontSize: 12, color: '#374151', fontWeight: 600 }}>{t.titulo}</div>
        </div>
      ))}
    </div>
  );
}
