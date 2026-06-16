// src/modules/asignaciones/components/AsignarDocenteModal.jsx

import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import docentesAsesoresService from '../../docentes/services/docentesAsesoresService';
import { useVinculacionDocumentos } from '../../vinculacion/hooks/useVinculacionDocumentos';
import { Button, Modal, Select } from '@/shared/components/ui';

export default function AsignarDocenteModal({ isOpen, asignacion, onClose, onConfirmar, isPending }) {
  // null = sin override del usuario todavia; usar el valor que llega del backend.
  const [seleccionManual, setSeleccionManual] = useState(null);

  const { detalle, isLoading: cargandoDetalle } = useVinculacionDocumentos(
    isOpen ? asignacion?.id : undefined
  );

  const { data: docentes = [] } = useQuery({
    queryKey: ['docentes-asesores-activos'],
    queryFn: () => docentesAsesoresService.listarPorPrograma().then((res) => res.filter((d) => d.activo)),
    enabled: isOpen,
  });

  const docenteActualId = detalle?.docenteAsesorId ? String(detalle.docenteAsesorId) : '';
  const docenteAsesorId = seleccionManual ?? docenteActualId;

  if (!isOpen || !asignacion) return null;

  const handleClose = () => {
    setSeleccionManual(null);
    onClose();
  };

  const handleSubmit = () => {
    if (!docenteAsesorId) return;
    onConfirmar(Number(docenteAsesorId));
  };

  return (
    <Modal
      titulo="Asignar Docente Asesor"
      onCerrar={handleClose}
      acciones={
        <div className="flex justify-end gap-2">
          <Button variant="ghost" size="sm" onClick={handleClose} disabled={isPending}>
            Cerrar
          </Button>
          <Button size="sm" onClick={handleSubmit} disabled={isPending || !docenteAsesorId}>
            {isPending ? 'Guardando...' : 'Guardar'}
          </Button>
        </div>
      }
    >
      <div className="mb-4 rounded-lg border border-gray-200 bg-slate-50 p-3 text-sm text-gray-700">
        <p>
          <strong>Estudiante:</strong> {asignacion.estudiante?.nombre}
        </p>
        <p>
          <strong>Cargo / Empresa:</strong> {asignacion.vacante?.cargo} · {asignacion.vacante?.empresa}
        </p>
      </div>

      <label htmlFor="docente-asesor-select" className="mb-1 block text-sm font-medium text-gray-700">
        Docente Asesor
      </label>
      <Select
        id="docente-asesor-select"
        value={docenteAsesorId}
        onChange={(e) => setSeleccionManual(e.target.value)}
        disabled={cargandoDetalle}
      >
        <option value="">Seleccione un docente...</option>
        {docentes.map((d) => (
          <option key={d.id} value={d.id}>
            {d.nombreCompleto || d.nombre}
          </option>
        ))}
      </Select>
      <p className="mt-1 text-xs text-gray-500">
        {detalle?.docenteAsesorId
          ? 'Ya tiene un docente asignado (posiblemente de forma automática); puedes cambiarlo.'
          : 'Aún no tiene docente asesor asignado.'}
      </p>
    </Modal>
  );
}