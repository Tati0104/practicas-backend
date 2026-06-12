// src/modules/asignaciones/components/AsignacionesTabla.jsx

/**
 * Tabla desktop para listar asignaciones.
 * Reutiliza TablaBase del shared.
 * Muestra columnas: estudiante, programa, vacante (cargo + empresa), fecha, estado, acciones.
 * Las acciones (ver detalle, cancelar) se reciben por props para mantener la tabla sin lógica de negocio.
 */
import { useNavigate } from 'react-router-dom';
import TablaBase from '../../../shared/components/TablaBase';
import BadgeAsignacion from './BadgeAsignacion';

export default function AsignacionesTabla({ asignaciones, isLoading, onCancelar, canCancelar }) {
  const navigate = useNavigate();

  // Definición de columnas para TablaBase
  const columnas = [
    {
      key: 'estudiante',
      titulo: 'Estudiante',
      render: (fila) => (
        <div>
          <div style={{ fontWeight: 600, fontSize: 13 }}>
            {fila.estudiante?.nombre || '—'}
          </div>
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
      key: 'fechaAsignacion',
      titulo: 'Fecha',
      render: (fila) =>
        fila.fechaAsignacion
          ? new Date(fila.fechaAsignacion).toLocaleDateString('es-CO')
          : '—',
    },
    {
      key: 'estado',
      titulo: 'Estado',
      render: (fila) => <BadgeAsignacion estado={fila.estado} />,
    },
    {
      key: 'acciones',
      titulo: 'Acciones',
      render: (fila) => (
        <div style={{ display: 'flex', gap: 6 }}>
          {/* Ver detalle */}
          <button
            onClick={() => navigate(`/asignaciones/${fila.id}`)}
            style={estilos.btnVer}
          >
            Ver
          </button>

          {/* Cancelar solo si el estado lo permite y el usuario tiene permiso */}
          {canCancelar && fila.estado !== 'CANCELADA' && fila.estado !== 'VINCULADA' && (
            <button
              onClick={() => onCancelar(fila)}
              style={estilos.btnCancelar}
            >
              Cancelar
            </button>
          )}
        </div>
      ),
    },
  ];

  return (
    <TablaBase
      columnas={columnas}
      datos={asignaciones}
      cargando={isLoading}
      sinDatos="No hay asignaciones que coincidan con los filtros."
    />
  );
}

const estilos = {
  btnVer: {
    padding: '4px 10px',
    fontSize: 12,
    background: '#2563eb',
    color: '#fff',
    border: 'none',
    borderRadius: 5,
    cursor: 'pointer',
  },
  btnCancelar: {
    padding: '4px 10px',
    fontSize: 12,
    background: '#fee2e2',
    color: '#991b1b',
    border: '1px solid #fca5a5',
    borderRadius: 5,
    cursor: 'pointer',
  },
};
