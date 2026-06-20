import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import docentesAsesoresService from '../../docentes/services/docentesAsesoresService';
import { Modal, Button, Input } from '@/shared/components/ui';

export default function ActivarPracticaModal({ isOpen, onClose, onConfirmar, isPending }) {
  const [fechaInicio, setFechaInicio] = useState('');
  const [fechaFin, setFechaFin] = useState('');
  const [docenteAsesorId, setDocenteAsesorId] = useState('');

  const { data: docentes = [] } = useQuery({
    queryKey: ['docentes-asesores-activos'],
    queryFn: () => docentesAsesoresService.listarPorPrograma().then(res => res.filter(d => d.activo)),
    enabled: isOpen
  });

  const handleSubmit = () => {
    onConfirmar({ fechaInicio, fechaFin, docenteAsesorId: Number(docenteAsesorId) });
  };

  const formIncompleto = !fechaInicio || !fechaFin || !docenteAsesorId;

  if (!isOpen) return null;

  return (
    <Modal
      titulo="Activar Práctica"
      onCerrar={onClose}
      acciones={
        <div className="flex justify-end gap-2 mt-4">
          <Button variant="ghost" size="sm" onClick={onClose} disabled={isPending}>
            Cancelar
          </Button>
          <Button size="sm" onClick={handleSubmit} disabled={isPending || formIncompleto}>
            {isPending ? 'Activando...' : 'Confirmar Activación'}
          </Button>
        </div>
      }
    >
      <div className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700">Fecha de Inicio</label>
          <Input
            type="date"
            value={fechaInicio}
            onChange={(e) => setFechaInicio(e.target.value)}
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700">Fecha de Fin</label>
          <Input
            type="date"
            value={fechaFin}
            onChange={(e) => setFechaFin(e.target.value)}
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700">Docente Asesor</label>
          <select
            className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm p-2 border"
            value={docenteAsesorId}
            onChange={(e) => setDocenteAsesorId(e.target.value)}
          >
            <option value="">Seleccione un docente...</option>
            {docentes.map(d => (
              <option key={d.id} value={d.id}>{d.nombreCompleto || d.nombre} - {d.correo}</option>
            ))}
          </select>
          <p className="mt-1 text-xs text-gray-500">
            Se muestran los docentes asesores activos.
          </p>
        </div>
      </div>
    </Modal>
  );
}
