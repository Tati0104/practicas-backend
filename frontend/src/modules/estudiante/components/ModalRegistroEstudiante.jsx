import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import configuracionService from '../../configuracion/services/configuracionService';
import { MOCK_PROGRAMAS } from '@/shared/mocks/datos';
import { ejecutarConsulta, usarMocks } from '@/shared/config/dataSource';
import { Button, Input, Modal, Select } from '@/shared/components/ui';

export default function ModalRegistroEstudiante({ onGuardar, onCerrar, guardando = false }) {
  const [form, setForm] = useState({
    nombre: '',
    identificacion: '',
    correo: '',
    telefono: '',
    contactoEmergencia: '',
    programaId: '',
    semestre: '',
    creditosAprobados: '',
    promedioAcumulado: '',
  });
  const [error, setError] = useState('');
  const campo = (k, v) => setForm((f) => ({ ...f, [k]: v }));

  const { data: programas = [] } = useQuery({
    queryKey: ['programas', usarMocks()],
    queryFn: () =>
      ejecutarConsulta({
        mock: () => MOCK_PROGRAMAS,
        api: () => configuracionService.listarProgramas().then((r) => r.data ?? []),
      }),
  });

  const guardar = () => {
    if (!form.nombre.trim()) {
      setError('El nombre es obligatorio');
      return;
    }
    if (!form.identificacion.trim()) {
      setError('La identificación es obligatoria');
      return;
    }
    if (!form.correo.trim()) {
      setError('El correo es obligatorio');
      return;
    }
    if (!form.programaId) {
      setError('El programa es obligatorio');
      return;
    }
    setError('');
    const dto = {
      nombre: form.nombre.trim(),
      identificacion: form.identificacion.trim(),
      correo: form.correo.trim(),
      telefono: form.telefono.trim() || null,
      contactoEmergencia: form.contactoEmergencia.trim() || null,
      programaId: Number(form.programaId),
      semestre: form.semestre ? Number(form.semestre) : null,
      creditosAprobados: form.creditosAprobados ? Number(form.creditosAprobados) : null,
      promedioAcumulado: form.promedioAcumulado ? Number(form.promedioAcumulado) : null,
    };
    onGuardar(dto);
  };

  return (
    <Modal
      titulo="Registrar estudiante"
      onCerrar={onCerrar}
      ancho="max-w-xl"
      acciones={
        <div className="mt-5 flex justify-end gap-2">
          <Button variant="secondary" size="sm" onClick={onCerrar} disabled={guardando}>
            Cancelar
          </Button>
          <Button size="sm" onClick={guardar} disabled={guardando}>
            {guardando ? 'Registrando...' : 'Registrar'}
          </Button>
        </div>
      }
    >
      {error && (
        <p className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
          {error}
        </p>
      )}

      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
        <div>
          <label className="mb-1 block text-xs font-semibold text-gray-700">Nombre completo</label>
          <Input
            value={form.nombre}
            onChange={(e) => campo('nombre', e.target.value)}
            placeholder="Ej: Ana García"
          />
        </div>
        <div>
          <label className="mb-1 block text-xs font-semibold text-gray-700">Identificación</label>
          <Input
            value={form.identificacion}
            onChange={(e) => campo('identificacion', e.target.value)}
            placeholder="Ej: 1001234567"
          />
        </div>
        <div>
          <label className="mb-1 block text-xs font-semibold text-gray-700">Correo</label>
          <Input
            type="email"
            value={form.correo}
            onChange={(e) => campo('correo', e.target.value)}
            placeholder="correo@avh.edu.co"
          />
        </div>
        <div>
          <label className="mb-1 block text-xs font-semibold text-gray-700">Teléfono</label>
          <Input
            value={form.telefono}
            onChange={(e) => campo('telefono', e.target.value)}
            placeholder="Ej: 3001234567"
          />
        </div>
        <div>
          <label className="mb-1 block text-xs font-semibold text-gray-700">
            Contacto emergencia
          </label>
          <Input
            value={form.contactoEmergencia}
            onChange={(e) => campo('contactoEmergencia', e.target.value)}
            placeholder="Ej: 3009876543"
          />
        </div>
        <div>
          <label className="mb-1 block text-xs font-semibold text-gray-700">Programa</label>
          <Select
            value={form.programaId}
            onChange={(e) => campo('programaId', e.target.value)}
          >
            <option value="">— Selecciona un programa —</option>
            {programas.filter((p) => p.activo !== false).map((p) => (
              <option key={p.id} value={p.id}>
                {p.nombre}
              </option>
            ))}
          </Select>
        </div>
        <div>
          <label className="mb-1 block text-xs font-semibold text-gray-700">Semestre</label>
          <Input
            type="number"
            value={form.semestre}
            onChange={(e) => campo('semestre', e.target.value)}
            placeholder="Ej: 8"
          />
        </div>
        <div>
          <label className="mb-1 block text-xs font-semibold text-gray-700">
            Créditos aprobados
          </label>
          <Input
            type="number"
            value={form.creditosAprobados}
            onChange={(e) => campo('creditosAprobados', e.target.value)}
            placeholder="Ej: 120"
          />
        </div>
        <div>
          <label className="mb-1 block text-xs font-semibold text-gray-700">
            Promedio acumulado
          </label>
          <Input
            type="number"
            value={form.promedioAcumulado}
            onChange={(e) => campo('promedioAcumulado', e.target.value)}
            placeholder="Ej: 3.8"
          />
        </div>
      </div>
    </Modal>
  );
}
