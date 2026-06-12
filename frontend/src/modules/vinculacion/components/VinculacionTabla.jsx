// src/modules/vinculacion/components/VinculacionTabla.jsx

/**
 * Componente: VinculacionTabla
 * ─────────────────────────────
 * Tabla desktop para listar los procesos de vinculación activos.
 * Se usa en pantallas ≥ 1280px. En móvil se usa VinculacionCard en su lugar.
 *
 * Reutiliza TablaBase del shared para mantener el estilo consistente con el resto
 * de módulos (VacantesTabla, AsignacionesTabla).
 *
 * Columnas mostradas:
 *   1. Estudiante     — nombre, código y programa
 *   2. Vacante        — cargo + empresa
 *   3. Carta          — badge de estado + progreso de firmas
 *   4. Convenio       — badge de estado + progreso de firmas
 *   5. Progreso total — X/2 documentos completados
 *   6. Acciones       — botón "Gestionar" → navega a la página de detalle
 *
 * Props:
 *   vinculaciones  → array de objetos de vinculación con sus documentos
 *   isLoading      → boolean — muestra "Cargando..." en TablaBase
 *   onGestionar    → function(vinculacion) — navega al detalle
 */

import { useNavigate } from 'react-router-dom';
import TablaBase    from '../../../shared/components/TablaBase';
import BadgeDocumento from './BadgeDocumento';

export default function VinculacionTabla({ vinculaciones, isLoading, onGestionar }) {
  const navigate = useNavigate();

  /**
   * Calcula cuántos documentos de la vinculación están en estado FIRMADO.
   * Sirve para mostrar el progreso global "X/2".
   */
  const contarFirmados = (docs = []) =>
    docs.filter((d) => d.estado === 'FIRMADO').length;

  // ── Definición de columnas para TablaBase ─────────────────────────────────
  const columnas = [
    {
      key: 'estudiante',
      titulo: 'Estudiante',
      render: (fila) => (
        <div>
          {/* Nombre principal */}
          <div style={{ fontWeight: 600, fontSize: 13 }}>
            {fila.estudiante?.nombre || '—'}
          </div>
          {/* Código y programa como subtexto */}
          <div style={{ fontSize: 11, color: '#6b7280' }}>
            {fila.estudiante?.codigo} · {fila.estudiante?.programa}
          </div>
        </div>
      ),
    },
    {
      key: 'vacante',
      titulo: 'Cargo / Empresa',
      render: (fila) => (
        <div>
          <div style={{ fontWeight: 600, fontSize: 13 }}>
            {fila.vacante?.cargo || '—'}
          </div>
          <div style={{ fontSize: 11, color: '#6b7280' }}>
            {fila.vacante?.empresa}
          </div>
        </div>
      ),
    },
    {
      key: 'carta',
      titulo: 'Carta',
      render: (fila) => {
        // Buscamos el documento de tipo CARTA en el array de documentos
        const carta = fila.documentos?.find((d) => d.tipo === 'CARTA');
        return carta ? (
          <BadgeDocumento estado={carta.estado} />
        ) : (
          <BadgeDocumento estado="PENDIENTE" />
        );
      },
    },
    {
      key: 'convenio',
      titulo: 'Convenio',
      render: (fila) => {
        const convenio = fila.documentos?.find((d) => d.tipo === 'CONVENIO');
        return convenio ? (
          <BadgeDocumento estado={convenio.estado} />
        ) : (
          <BadgeDocumento estado="PENDIENTE" />
        );
      },
    },
    {
      key: 'progreso',
      titulo: 'Progreso',
      render: (fila) => {
        const firmados = contarFirmados(fila.documentos);
        const total    = fila.documentos?.length || 2;
        return (
          <span style={{ fontSize: 13, fontWeight: 600, color: firmados === total ? '#059669' : '#374151' }}>
            {firmados}/{total} firmados
          </span>
        );
      },
    },
    {
      key: 'acciones',
      titulo: 'Acciones',
      render: (fila) => (
        <button
          onClick={() =>
            onGestionar
              ? onGestionar(fila)
              : navigate(`/vinculacion/${fila.practicaId}`)
          }
          style={estilos.btnGestionar}
          aria-label={`Gestionar vinculación de ${fila.estudiante?.nombre}`}
        >
          Gestionar
        </button>
      ),
    },
  ];

  return (
    <TablaBase
      columnas={columnas}
      datos={vinculaciones}
      cargando={isLoading}
      sinDatos="No hay procesos de vinculación que coincidan con los filtros."
    />
  );
}

// ── Estilos ───────────────────────────────────────────────────────────────────
const estilos = {
  btnGestionar: {
    padding: '4px 12px',
    fontSize: 12,
    background: '#2563eb',
    color: '#fff',
    border: 'none',
    borderRadius: 5,
    cursor: 'pointer',
    fontWeight: 600,
  },
};
