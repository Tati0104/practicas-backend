// src/modules/vinculacion/components/ProgresoFirmas.jsx

/**
 * Componente: ProgresoFirmas
 * ─────────────────────────
 * Muestra el estado visual de las firmas de un documento de vinculación.
 *
 * ¿Qué hace?
 *   - Itera sobre el array `firmas` y dibuja un círculo por cada firmante.
 *   - Círculo VERDE con "✓"  → firmante ya confirmó la firma.
 *   - Círculo GRIS con "○"   → aún pendiente de firma.
 *   - Debajo de cada círculo, muestra la abreviatura del rol (Coord. / Tutor / Estud.).
 *   - En la parte superior muestra "X/3 firmas" como resumen rápido.
 *
 * Props:
 *   firmas: Array<{
 *     tipoFirmante: 'COORDINADOR' | 'TUTOR' | 'ESTUDIANTE',
 *     firmado: boolean,
 *     fechaFirma: string | null   ← fecha ISO si firmado, null si pendiente
 *   }>
 *
 * Uso:
 *   <ProgresoFirmas firmas={documento.firmas} />
 */

// Mapa de código de firmante → etiqueta corta para mostrar bajo el círculo
const ETIQUETAS = {
  COORDINADOR: 'Coord.',
  TUTOR:       'Tutor',
  ESTUDIANTE:  'Estud.',
};

export default function ProgresoFirmas({ firmas = [] }) {
  // Contamos cuántos firmantes ya confirmaron
  const totalFirmadas = firmas.filter((f) => f.firmado).length;
  const total = firmas.length;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>

      {/* ── Resumen numérico: "X/3 firmas" ── */}
      <div style={{ fontSize: 12, fontWeight: 600, color: '#374151' }}>
        {totalFirmadas}/{total} firmas
      </div>

      {/* ── Círculos individuales por firmante ── */}
      <div style={{ display: 'flex', gap: 8 }}>
        {firmas.map((f) => (
          <div
            key={f.tipoFirmante}
            style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2 }}
          >
            {/*
              Círculo indicador:
                - title muestra tooltip con la fecha de firma (si aplica)
                - verde + "✓" cuando firmado
                - gris  + "○" cuando pendiente
            */}
            <div
              title={
                f.firmado
                  ? `Firmado el ${new Date(f.fechaFirma).toLocaleDateString('es-CO')}`
                  : 'Pendiente de firma'
              }
              style={{
                width: 28,
                height: 28,
                borderRadius: '50%',
                background: f.firmado ? '#d1fae5' : '#f3f4f6',
                border: `2px solid ${f.firmado ? '#059669' : '#d1d5db'}`,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: 14,
                cursor: 'default',
              }}
            >
              {f.firmado ? '✓' : '○'}
            </div>

            {/* Etiqueta del rol debajo del círculo */}
            <span style={{ fontSize: 10, color: '#6b7280' }}>
              {ETIQUETAS[f.tipoFirmante] || f.tipoFirmante}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}
