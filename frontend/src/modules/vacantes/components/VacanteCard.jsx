// src/modules/vacantes/components/VacanteCard.jsx

/**
 * Card visual para dispositivos móviles que muestra la información básica de una vacante.
 * Se reutilizan los componentes compartidos `BadgeEstado` y los botones de acción.
 * Las acciones (aprobar, rechazar, pausar, cerrar) se habilitan según los permisos
 * recibidos en la prop `acciones`.
 */
import { BadgeEstado } from '../../../shared/components/BadgeEstado';
import { toast } from 'react-hot-toast';

export default function VacanteCard({ vacante, acciones }) {
  const {
    aprobar,
    rechazar,
    pausar,
    cerrar,
    canApprove,
    canReject,
    canPause,
    canClose,
  } = acciones;

  const handleAprobar = () => {
    aprobar.mutate(vacante.id);
  };
  const handleRechazar = () => {
    // abrir modal de rechazo (se delega a componente padre vía estado externo)
    // aquí solo lanzamos la mutación directa si el modal no es necesario
    rechazar.mutate({ id: vacante.id, motivo: 'Rechazado por UI' });
  };
  const handlePausar = () => pausar.mutate(vacante.id);
  const handleCerrar = () => cerrar.mutate(vacante.id);

  return (
    <div className="border rounded-lg p-4 shadow-sm bg-white">
      <h3 className="font-semibold text-lg text-gray-800">{vacante.cargo}</h3>
      <p className="text-sm text-gray-600">{vacante.empresa}</p>
      <p className="text-sm text-gray-600">{vacante.modalidad}</p>
      <p className="text-sm text-gray-600">
        Cupos: {vacante.cuposDisponibles} / {vacante.cuposTotal}
      </p>
      <BadgeEstado estado={vacante.estado} />
      <div className="mt-3 flex flex-wrap gap-2">
        {canApprove && (
          <button onClick={handleAprobar} className="px-2 py-1 text-xs bg-green-100 text-green-800 rounded">
            Aprobar
          </button>
        )}
        {canReject && (
          <button onClick={handleRechazar} className="px-2 py-1 text-xs bg-red-100 text-red-800 rounded">
            Rechazar
          </button>
        )}
        {canPause && (
          <button onClick={handlePausar} className="px-2 py-1 text-xs bg-yellow-100 text-yellow-800 rounded">
            Pausar
          </button>
        )}
        {canClose && (
          <button onClick={handleCerrar} className="px-2 py-1 text-xs bg-gray-100 text-gray-800 rounded">
            Cerrar
          </button>
        )}
      </div>
    </div>
  );
}
