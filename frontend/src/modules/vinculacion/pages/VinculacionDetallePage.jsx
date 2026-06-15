// src/modules/vinculacion/pages/VinculacionDetallePage.jsx

/**
 * Página: VinculacionDetallePage
 * ────────────────────────────────
 * Página de detalle de un proceso de vinculación para una práctica específica.
 *
 * ¿Qué muestra?
 *   - Datos del estudiante (nombre, código, programa).
 *   - Datos de la vacante (cargo, empresa, modalidad).
 *   - Panel de la CARTA DE PRESENTACIÓN:
 *       · Estado (badge)
 *       · Dropzone para subir (si aún no está subida y el rol puede)
 *       · Progreso de firmas (3 círculos)
 *       · Botón "Confirmar mi firma" según el rol del usuario
 *   - Panel del CONVENIO DE PRÁCTICA:
 *       · Igual que la carta
 *   - Indicador de progreso total ("X/3 firmas — X/2 documentos")
 *   - Botón "Activar práctica" — habilitado SOLO cuando los 2 documentos tienen
 *     las 3 firmas confirmadas (estado FIRMADO en ambos).
 *
 * ¿Cómo funciona internamente?
 *   1. Lee el `practicaId` de los parámetros de la URL.
 *   2. Llama a useVinculacionDocumentos(practicaId) para obtener los documentos.
 *   3. Llama a useVinculacionMutaciones para tener subir, confirmarFirma.
 *   4. El estado `firmaSeleccionada` guarda qué documento/firmante está pendiente
 *      de confirmación en el modal ConfirmarFirmaModal.
 *
 * Roles:
 *   - COORD_PRACTICA  → puede subir ambos documentos y firmar como COORDINADOR
 *   - TUTOR_EMPRESARIAL → puede firmar como TUTOR
 *   - ESTUDIANTE      → puede firmar como ESTUDIANTE
 *   (En este stub, usePermisos devuelve todo true)
 *
 * URL: /vinculacion/:practicaId
 */

import { useState }          from 'react';
import { useParams, useNavigate } from 'react-router-dom';

import { useVinculacionDocumentos } from '../hooks/useVinculacionDocumentos';
import { useVinculacionMutaciones } from '../hooks/useVinculacionMutaciones';
import { usePermisos }              from '../../../shared/hooks/usePermisos';

import PanelDocumento      from '../components/PanelDocumento';
import PanelDocumentoBase  from '../components/PanelDocumentoBase';
import ConfirmarFirmaModal from '../components/ConfirmarFirmaModal';

export default function VinculacionDetallePage() {
  const { practicaId } = useParams();
  const navigate        = useNavigate();

  // ── Estado del modal de confirmación de firma ──────────────────────────────
  // Guardamos { documento, tipoFirmante } cuando el usuario hace clic en "Confirmar mi firma"
  const [firmaSeleccionada, setFirmaSeleccionada] = useState(null);

  // ── Datos y mutaciones ─────────────────────────────────────────────────────
  const { documentos, estudiante, isLoading, isError, refetch } =
    useVinculacionDocumentos(practicaId);

  const { subirCarta, subirConvenio, confirmarFirma } = useVinculacionMutaciones({
    practicaId,
    // Al terminar cualquier operación exitosa, recargamos los documentos
    onSuccess: () => {
      refetch();
      setFirmaSeleccionada(null); // Cierra el modal de firma si estaba abierto
    },
  });

  // Permisos del usuario actual
  const { canCreate } = usePermisos();

  // ── Cálculo de progreso total ──────────────────────────────────────────────
  // Un documento está "completo" cuando su estado es FIRMADO
  const documentosFirmados = documentos.filter((d) => d.estado === 'FIRMADO').length;
  const totalDocumentos    = documentos.length;

  // La práctica puede activarse solo cuando TODOS los documentos están firmados
  const puedeActivar = totalDocumentos > 0 && documentosFirmados === totalDocumentos;

  // ── Separamos los documentos por tipo ─────────────────────────────────────
  const carta    = documentos.find((d) => d.tipo === 'CARTA');
  const convenio = documentos.find((d) => d.tipo === 'CONVENIO');

  // ── Handlers ──────────────────────────────────────────────────────────────

  /**
   * Sube un archivo según el tipo del documento.
   * asignacionId: necesario para construir el endpoint correcto.
   */
  const handleSubir = (tipo, asignacionId, archivo) => {
    if (tipo === 'CARTA') {
      subirCarta.mutate({ asignacionId, archivo });
    } else {
      subirConvenio.mutate({ asignacionId, archivo });
    }
  };

  /**
   * Abre el modal de confirmación de firma.
   * Guardamos el documento y el tipo de firmante en el estado.
   */
  const handleAbrirFirma = (documento, tipoFirmante) => {
    setFirmaSeleccionada({ documento, tipoFirmante });
  };

  /**
   * Confirma la firma después de que el usuario aprueba en el modal.
   * Usa el ID del documento (convenio o carta) y el tipo de firmante.
   */
  const handleConfirmarFirma = () => {
    if (!firmaSeleccionada) return;
    confirmarFirma.mutate({
      convenioId:    firmaSeleccionada.documento.id,
      tipoFirmante:  firmaSeleccionada.tipoFirmante,
    });
  };

  /**
   * Descarga un documento llamando al servicio.
   * Abre el binario como un blob y lo descarga con un <a> temporal.
   */
  const handleDescargar = async (documentoId, nombre) => {
    try {
      // Importamos el servicio de forma dinámica para no importar http.js aquí directamente
      const { default: svc } = await import('../services/vinculacionService');
      const resp = await svc.descargarDocumento(documentoId);
      // Creamos una URL temporal para el blob y simulamos un clic de descarga
      const url  = URL.createObjectURL(resp.data);
      const link = document.createElement('a');
      link.href     = url;
      link.download = nombre || `documento_${documentoId}.pdf`;
      link.click();
      URL.revokeObjectURL(url); // Libera memoria
    } catch {
      // El error ya se maneja con toast en el servicio
    }
  };

  // ── Render: estado de carga ────────────────────────────────────────────────
  if (isLoading) {
    return (
      <div style={estilos.centrado}>
        <p style={{ color: '#6b7280' }}>Cargando documentos de la práctica...</p>
      </div>
    );
  }

  // ── Render: estado de error ────────────────────────────────────────────────
  if (isError) {
    return (
      <div style={estilos.centrado}>
        <div style={estilos.errorBox}>
          <p>No se pudieron cargar los documentos. Intenta de nuevo.</p>
          <button onClick={refetch} style={estilos.btnVolver}>
            Reintentar
          </button>
        </div>
      </div>
    );
  }

  // ── Render principal ───────────────────────────────────────────────────────
  return (
    <div style={{ maxWidth: 800, margin: '0 auto', padding: 24, fontFamily: 'Arial, sans-serif' }}>

      {/* Botón de regreso */}
      <button
        onClick={() => navigate('/vinculacion')}
        style={estilos.btnVolver}
        aria-label="Volver al listado de vinculación"
      >
        ← Volver
      </button>

      {/* Título de la página */}
      <h1 style={{ fontSize: 22, fontWeight: 800, color: '#111827', margin: '16px 0 4px' }}>
        Gestión de Documentos
      </h1>
      <p style={{ fontSize: 13, color: '#6b7280', marginBottom: 20 }}>
        Práctica #{practicaId} — Sube y firma los documentos requeridos para activar la práctica.
      </p>

      {/* ── Indicador de progreso global ── */}
      <div style={estilos.progresoGlobal}>
        <span style={{
          ...estilos.progresoTexto,
          color: puedeActivar ? '#059669' : '#374151',
        }}>
          {puedeActivar ? '✅' : '⏳'} {documentosFirmados}/{totalDocumentos} documentos firmados
        </span>
        {puedeActivar && (
          <span style={{ fontSize: 12, color: '#059669', fontWeight: 600 }}>
            ¡Todos los documentos están firmados!
          </span>
        )}
      </div>

      {/* ── Paneles de documentos (uno por tipo) ── */}
      <div style={estilos.gridDocumentos}>

        {/* Panel Carta de Presentación */}
        {carta ? (
          <PanelDocumento
            documento={carta}
            asignacionId={practicaId} // Usamos practicaId como proxy de asignacionId
            onSubir={(asignacionId, archivo) => handleSubir('CARTA', asignacionId, archivo)}
            onDescargar={() => handleDescargar(carta.id, carta.nombre)}
            onFirmar={(tipoFirmante) => handleAbrirFirma(carta, tipoFirmante)}
            isPendingSubir={subirCarta.isPending}
            isPendingFirma={confirmarFirma.isPending}
            puedeSubir={canCreate}
            tipoFirmanteRol="COORDINADOR" // En producción viene del contexto de auth
          />
        ) : (
          /* Si no hay carta en la respuesta, mostramos un panel vacío */
          <div style={estilos.panelVacio}>
            <p style={{ color: '#9ca3af', fontSize: 13 }}>
              Carta de Presentación — no disponible aún
            </p>
          </div>
        )}

        {/* Panel Convenio de Práctica */}
        {convenio ? (
          <PanelDocumento
            documento={convenio}
            asignacionId={practicaId}
            onSubir={(asignacionId, archivo) => handleSubir('CONVENIO', asignacionId, archivo)}
            onDescargar={() => handleDescargar(convenio.id, convenio.nombre)}
            onFirmar={(tipoFirmante) => handleAbrirFirma(convenio, tipoFirmante)}
            isPendingSubir={subirConvenio.isPending}
            isPendingFirma={confirmarFirma.isPending}
            puedeSubir={canCreate}
            tipoFirmanteRol="COORDINADOR"
          />
        ) : (
          <div style={estilos.panelVacio}>
            <p style={{ color: '#9ca3af', fontSize: 13 }}>
              Convenio de Práctica — no disponible aún
            </p>
          </div>
        )}

        {/* Panel Hoja de Vida */}
        <PanelDocumentoBase
          tipo="HOJA_DE_VIDA"
          estudiante={estudiante}
          puedeSubir={canCreate}
          onSubidoExitosamente={refetch}
        />

        {/* Panel Paz y Salvo */}
        <PanelDocumentoBase
          tipo="PAZ_Y_SALVO"
          estudiante={estudiante}
          puedeSubir={canCreate}
          onSubidoExitosamente={refetch}
        />
      </div>

      {/* ── Botón "Activar práctica" ── */}
      {/*
        Solo se habilita cuando todos los documentos están en estado FIRMADO.
        Cuando no está habilitado, muestra un mensaje explicativo.
      */}
      <div style={estilos.seccionActivar}>
        {!puedeActivar && (
          <p style={estilos.advertenciaActivar}>
            🔒 Para activar la práctica se requieren todas las firmas en ambos documentos.
          </p>
        )}
        <button
          type="button"
          disabled={!puedeActivar}
          style={{
            ...estilos.btnActivar,
            opacity: puedeActivar ? 1 : 0.4,
            cursor:  puedeActivar ? 'pointer' : 'not-allowed',
          }}
          aria-label="Activar práctica (requiere todos los documentos firmados)"
          onClick={() => {
            // TODO: Conectar con el endpoint de activación de práctica cuando esté disponible
            // Por ahora mostramos un toast informativo
            import('react-hot-toast').then(({ toast }) => {
              toast.success('Práctica activada exitosamente');
            });
          }}
        >
          🚀 Activar práctica
        </button>
      </div>

      {/* ── Modal de confirmación de firma ── */}
      <ConfirmarFirmaModal
        isOpen={!!firmaSeleccionada}
        documento={firmaSeleccionada?.documento}
        tipoFirmante={firmaSeleccionada?.tipoFirmante}
        onClose={() => setFirmaSeleccionada(null)}
        onConfirmar={handleConfirmarFirma}
        isPending={confirmarFirma.isPending}
      />
    </div>
  );
}

// ── Estilos ───────────────────────────────────────────────────────────────────
const estilos = {
  centrado: {
    display: 'flex', justifyContent: 'center',
    alignItems: 'center', minHeight: 200,
    flexDirection: 'column', gap: 12,
  },
  errorBox: {
    background: '#fee2e2', border: '1px solid #fca5a5',
    borderRadius: 8, padding: 20, textAlign: 'center', color: '#991b1b',
  },
  btnVolver: {
    padding: '6px 14px', background: '#f1f5f9',
    border: '1px solid #d1d5db', borderRadius: 6,
    fontSize: 13, cursor: 'pointer', color: '#374151',
  },
  progresoGlobal: {
    background: '#f8fafc', border: '1px solid #e5e7eb',
    borderRadius: 8, padding: '10px 16px',
    display: 'flex', alignItems: 'center', gap: 12,
    marginBottom: 20,
  },
  progresoTexto: { fontSize: 14, fontWeight: 700 },
  // Grid de 2 columnas en desktop, 1 columna en móvil
  gridDocumentos: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
    gap: 16,
    marginBottom: 24,
  },
  panelVacio: {
    background: '#f9fafb', border: '1px dashed #d1d5db',
    borderRadius: 10, padding: 24, textAlign: 'center',
  },
  seccionActivar: {
    borderTop: '1px solid #e5e7eb',
    paddingTop: 20,
    display: 'flex', flexDirection: 'column', alignItems: 'flex-start', gap: 8,
  },
  advertenciaActivar: {
    fontSize: 12, color: '#92400e',
    background: '#fef9c3', border: '1px solid #fde68a',
    borderRadius: 6, padding: '8px 12px', margin: 0,
  },
  btnActivar: {
    padding: '10px 24px',
    background: '#059669', color: '#fff',
    border: 'none', borderRadius: 8,
    fontSize: 14, fontWeight: 700,
  },
};
