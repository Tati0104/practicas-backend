import { useState, useEffect } from 'react';
import empresaService from '../services/empresaService';
import { Button, Input, Modal, Select } from '@/shared/components/ui';

const formularioVacio = {
  nit: '',
  razonSocial: '',
  sector: '',
  direccion: '',
  municipio: '',
  telefono: '',
};

export default function ModalEmpresa({ empresa, onGuardar, onCerrar, guardando = false }) {
  const esEdicion = Boolean(empresa?.id);
  const [form, setForm] = useState(formularioVacio);
  const [sectores, setSectores] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    empresaService
      .listarSectores()
      .then((res) => setSectores(res.data ?? []))
      .catch(() => setSectores([]));
  }, []);

  useEffect(() => {
    if (empresa) {
      setForm({
        nit: empresa.nit ?? '',
        razonSocial: empresa.razonSocial ?? '',
        sector: empresa.sector?.id ? String(empresa.sector.id) : '',
        direccion: empresa.direccion ?? '',
        municipio: empresa.municipio ?? '',
        telefono: empresa.telefono ?? '',
      });
    } else {
      setForm(formularioVacio);
    }
    setError('');
  }, [empresa]);

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
    const { sector: sectorId, ...resto } = form;
    onGuardar({ ...resto, sector: { id: Number(sectorId) } });
  };

  return (
    <Modal
      titulo={esEdicion ? 'Editar empresa' : 'Registrar empresa'}
      onCerrar={onCerrar}
      ancho="max-w-lg"
      acciones={
        <div className="mt-5 flex justify-end gap-2">
          <Button variant="secondary" size="sm" onClick={onCerrar} disabled={guardando}>
            Cancelar
          </Button>
          <Button size="sm" onClick={guardar} disabled={guardando}>
            {guardando ? 'Guardando...' : esEdicion ? 'Guardar cambios' : 'Registrar'}
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
            {sectores.map((s) => (
              <option key={s.id} value={s.id}>
                {s.nombre}
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
