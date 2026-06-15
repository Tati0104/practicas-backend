import { useState } from 'react';
import { Button, Input, Modal, Select } from '@/shared/components/ui';

const SECTORES = [
  'TECNOLOGIA',
  'CONSTRUCCION',
  'AGRICULTURA',
  'SALUD',
  'EDUCACION',
  'COMERCIO',
  'INDUSTRIA',
  'SERVICIOS',
];

export default function ModalEmpresa({ onGuardar, onCerrar }) {
  const [form, setForm] = useState({
    nit: '',
    razonSocial: '',
    sector: '',
    direccion: '',
    municipio: '',
    telefono: '',
  });
  const [error, setError] = useState('');
  const campo = (k, v) => setForm((f) => ({ ...f, [k]: v }));

  const guardar = () => {
    if (!form.nit.trim()) {
      setError('El NIT es obligatorio');
      return;
    }
    if (!form.razonSocial.trim()) {
      setError('La razón social es obligatoria');
      return;
    }
    if (!form.sector) {
      setError('El sector es obligatorio');
      return;
    }
    setError('');
    onGuardar(form);
  };

  return (
    <Modal
      titulo="Registrar empresa"
      onCerrar={onCerrar}
      ancho="max-w-lg"
      acciones={
        <div className="mt-5 flex justify-end gap-2">
          <Button variant="secondary" size="sm" onClick={onCerrar}>
            Cancelar
          </Button>
          <Button size="sm" onClick={guardar}>
            Registrar
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
          <label className="mb-1 block text-xs font-semibold text-gray-700">NIT</label>
          <Input
            value={form.nit}
            onChange={(e) => campo('nit', e.target.value)}
            placeholder="Ej: 900123456-1"
          />
        </div>
        <div>
          <label className="mb-1 block text-xs font-semibold text-gray-700">Razón social</label>
          <Input
            value={form.razonSocial}
            onChange={(e) => campo('razonSocial', e.target.value)}
            placeholder="Nombre de la empresa"
          />
        </div>
        <div>
          <label className="mb-1 block text-xs font-semibold text-gray-700">Sector</label>
          <Select value={form.sector} onChange={(e) => campo('sector', e.target.value)}>
            <option value="">Seleccionar</option>
            {SECTORES.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </Select>
        </div>
        <div>
          <label className="mb-1 block text-xs font-semibold text-gray-700">Municipio</label>
          <Input
            value={form.municipio}
            onChange={(e) => campo('municipio', e.target.value)}
            placeholder="Ej: Armenia"
          />
        </div>
        <div>
          <label className="mb-1 block text-xs font-semibold text-gray-700">Dirección</label>
          <Input
            value={form.direccion}
            onChange={(e) => campo('direccion', e.target.value)}
            placeholder="Ej: Calle 10 # 5-20"
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
      </div>
    </Modal>
  );
}
