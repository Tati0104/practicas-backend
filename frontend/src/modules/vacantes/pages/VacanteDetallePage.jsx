// src/modules/vacantes/pages/VacanteDetallePage.jsx

import { useParams } from 'react-router-dom';
import { useVacanteDetalle } from '../hooks/useVacanteDetalle';
import HistorialEstados from '../components/HistorialEstados';

export default function VacanteDetallePage() {
  const { id } = useParams();
  const { vacante, isLoading, isError } = useVacanteDetalle(id);

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <span className="text-gray-600">Cargando detalle de la vacante...</span>
      </div>
    );
  }

  if (isError) {
    return (
      <div className="p-4 bg-red-100 text-red-800 rounded">
        <p>Hubo un error al cargar la vacante.</p>
      </div>
    );
  }

  if (!vacante) {
    return (
      <div className="p-4">
        <p>No se encontrÃ³ la vacante.</p>
      </div>
    );
  }

  const {
    empresa,
    cargo,
    modalidad,
    cuposTotal,
    cuposDisponibles,
    estado,
    // any other fields can be added here
  } = vacante;

  return (
    <div className="max-w-4xl mx-auto p-6 space-y-6">
      <h1 className="text-2xl font-bold">Detalle de Vacante</h1>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 bg-white rounded shadow p-4">
        <div>
          <span className="font-medium">Empresa:</span> {empresa}
        </div>
        <div>
          <span className="font-medium">Cargo:</span> {cargo}
        </div>
        <div>
          <span className="font-medium">Modalidad:</span> {modalidad}
        </div>
        <div>
          <span className="font-medium">Estado:</span> {estado}
        </div>
        <div>
          <span className="font-medium">Cupos Totales:</span> {cuposTotal}
        </div>
        <div>
          <span className="font-medium">Cupos Disponibles:</span> {cuposDisponibles}
        </div>
      </div>

      {/* Historial de estados */}
      <div className="mt-6">
        <h2 className="text-xl font-semibold mb-2">Historial de Estados</h2>
        <HistorialEstados vacanteId={id} />
      </div>
    </div>
  );
}
