import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import docentesAsesoresService from '../../docentes/services/docentesAsesoresService';
import { Modal, Button, Input, Select } from '@/shared/components/ui';

function abrirCalendario(event) {
  if (typeof event.currentTarget.showPicker === 'function') {
    try {
      event.currentTarget.showPicker();
    } catch {
      // Algunos navegadores solo permiten showPicker tras gesto del usuario.
    }
  }
}

export default function ActivarPracticaModal({ isOpen, onClose, onConfirmar, isPending }) {
  const [fechaInicio, setFechaInicio] = useState('');
  const [fechaFin, setFechaFin] = useState('');
  const [docenteAsesorId, setDocenteAsesorId] = useState('');

  const { data: docentes = [] } = useQuery({
    queryKey: ['docentes-asesores-activos'],
    queryFn: () =>
      docentesAsesoresService.listarPorPrograma().then((res) => res.filter((d) => d.activo)),
    enabled: isOpen,
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
      ancho="max-w-lg"
      acciones={
        <div className="flex justify-end gap-2">
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
          <label htmlFor="fecha-inicio-practica" className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-slate-200">
            Fecha de inicio
          </label>
          <Input
            id="fecha-inicio-practica"
            type="date"
            value={fechaInicio}
            onChange={(e) => setFechaInicio(e.target.value)}
            onClick={abrirCalendario}
            onFocus={abrirCalendario}
          />
        </div>
        <div>
          <label htmlFor="fecha-fin-practica" className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-slate-200">
            Fecha de fin
          </label>
          <Input
            id="fecha-fin-practica"
            type="date"
            value={fechaFin}
            min={fechaInicio || undefined}
            onChange={(e) => setFechaFin(e.target.value)}
            onClick={abrirCalendario}
            onFocus={abrirCalendario}
          />
        </div>
        <div>
          <label htmlFor="docente-asesor-practica" className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-slate-200">
            Docente asesor
          </label>
          <Select
            id="docente-asesor-practica"
            value={docenteAsesorId}
            onChange={(e) => setDocenteAsesorId(e.target.value)}
          >
            <option value="">Seleccione un docente…</option>
            {docentes.map((d) => (
              <option key={d.id} value={d.id}>
                {d.nombreCompleto || d.nombre} — {d.correo}
              </option>
            ))}
          </Select>
          <p className="mt-1 text-xs text-gray-500 dark:text-slate-400">
            Se muestran los docentes asesores activos.
          </p>
        </div>
      </div>
    </Modal>
  );
}
