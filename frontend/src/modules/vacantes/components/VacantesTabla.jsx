// src/modules/vacantes/components/VacantesTabla.jsx
/**
 * Tabla de vacantes para vista desktop.
 * Utiliza el componente compartido TablaBase.jsx.
 * Recibe la lista de vacantes y un objeto `acciones` con mutaciones y flags de permiso.
 */
import TablaBase from '../../../shared/components/TablaBase';
import BadgeEstado from '../../../shared/components/BadgeEstado';
import { toast } from 'react-hot-toast';

export default function VacantesTabla({ vacantes, acciones }) {
  const { aprobar, rechazar, pausar, cerrar, canApprove, canReject, canPause, canClose } = acciones;

  const columnas = [
    { key: 'empresa',   titulo: 'Empresa' },
    { key: 'cargo',     titulo: 'Cargo' },
    { key: 'modalidad', titulo: 'Modalidad' },
    {
      key: 'cupos',
      titulo: 'Cupos',
      render: v => `${v.cuposDisponibles} / ${v.cuposTotal}`,
    },
    {
      key: 'estado',
      titulo: 'Estado',
      render: v => <BadgeEstado estado={v.estado} />, // reutiliza badge compartido
    },
    {
      key: 'acciones',
      titulo: 'Acciones',
      render: v => (
        <div className="flex gap-2">
          {canApprove && v.estado === 'PENDIENTE_APROBACION' && (
            <button
              onClick={() => aprobar.mutate(v.id)}
              className="bg-green-200 text-green-800 px-2 py-1 rounded text-xs"
            >Aprobar</button>
          )}
          {canReject && v.estado === 'PENDIENTE_APROBACION' && (
            <button
              onClick={() => rechazar.mutate({ id: v.id, motivo: 'Rechazado por admin' })}
              className="bg-red-200 text-red-800 px-2 py-1 rounded text-xs"
            >Rechazar</button>
          )}
          {canPause && v.estado === 'ACTIVA' && (
            <button
              onClick={() => pausar.mutate(v.id)}
              className="bg-yellow-200 text-yellow-800 px-2 py-1 rounded text-xs"
            >Pausar</button>
          )}
          {canClose && v.estado !== 'CERRADA' && (
            <button
              onClick={() => cerrar.mutate(v.id)}
              className="bg-gray-300 text-gray-800 px-2 py-1 rounded text-xs"
            >Cerrar</button>
          )}
        </div>
      ),
    },
  ];

  return <TablaBase columnas={columnas} datos={vacantes} />;
}
