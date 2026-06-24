import { useState, useEffect } from 'react';
import { Button, Input, Modal } from '@/shared/components/ui';

const formularioVacio = {
  nombre: '',
  correo: '',
  cargo: '',
  telefono: '',
};

export default function ModalRegistrarTutor({ empresa, onGuardar, onCerrar, guardando = false }) {
  const [form, setForm] = useState(formularioVacio);
  const [error, setError] = useState('');

  useEffect(() => {
    setForm(formularioVacio);
    setError('');
  }, [empresa?.id]);

  const campo = (key, value) => setForm((prev) => ({ ...prev, [key]: value }));

  const guardar = () => {
    if (!form.nombre.trim()) {
      setError('El nombre es obligatorio');
      return;
    }
    if (!form.correo.trim()) {
      setError('El correo es obligatorio');
      return;
    }
    if (!form.cargo.trim()) {
      setError('El cargo es obligatorio');
      return;
    }
    if (!form.telefono.trim()) {
      setError('El teléfono es obligatorio');
      return;
    }

    setError('');
    onGuardar({
      empresaId: empresa.id,
      empresa: { id: empresa.id },
      nombre: form.nombre.trim(),
      correo: form.correo.trim(),
      cargo: form.cargo.trim(),
      telefono: form.telefono.trim(),
    });
  };

  return (
    <Modal
      titulo="Agregar tutor empresarial"
      onCerrar={onCerrar}
      ancho="max-w-md"
      acciones={
        <div className="flex justify-end gap-2">
          <Button variant="secondary" size="sm" onClick={onCerrar} disabled={guardando}>
            Cancelar
          </Button>
          <Button size="sm" onClick={guardar} disabled={guardando}>
            {guardando ? 'Guardando…' : 'Registrar tutor'}
          </Button>
        </div>
      }
    >
      <p className="mb-4 text-sm ui-text-muted">
        El tutor quedará vinculado a <strong className="ui-text-body">{empresa?.razonSocial}</strong>.
        Se creará su usuario y recibirá una contraseña temporal por correo.
      </p>

      {error && (
        <p className="mb-3 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700 dark:border-red-500/30 dark:bg-red-500/10 dark:text-red-300">
          {error}
        </p>
      )}

      <div className="grid grid-cols-1 gap-3">
        <div>
          <label className="mb-1 block text-xs font-semibold ui-text-body">Nombre completo</label>
          <Input
            value={form.nombre}
            onChange={(e) => campo('nombre', e.target.value)}
            placeholder="Ej: María López"
          />
        </div>
        <div>
          <label className="mb-1 block text-xs font-semibold ui-text-body">Correo electrónico</label>
          <Input
            type="email"
            value={form.correo}
            onChange={(e) => campo('correo', e.target.value)}
            placeholder="tutor@empresa.com"
          />
        </div>
        <div>
          <label className="mb-1 block text-xs font-semibold ui-text-body">Cargo en la empresa</label>
          <Input
            value={form.cargo}
            onChange={(e) => campo('cargo', e.target.value)}
            placeholder="Ej: Jefe de recursos humanos"
          />
        </div>
        <div>
          <label className="mb-1 block text-xs font-semibold ui-text-body">Teléfono</label>
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
