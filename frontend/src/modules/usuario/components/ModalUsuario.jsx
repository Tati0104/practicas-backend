import { useState, useEffect } from 'react';
import { Button, Input, Modal, Select } from '@/shared/components/ui';

const ROLES = [
  'ADMIN',
  'DIRECCION',
  'COORD_ACADEMICA',
  'COORD_PRACTICA',
  'SECRETARIA',
  'DOCENTE_ASESOR',
  'EMPRESA',
  'TUTOR_EMPRESARIAL',
  'ESTUDIANTE',
];
const SCOPES = ['GLOBAL', 'FACULTAD', 'PROGRAMA', 'ASIGNADO'];

export default function ModalUsuario({ usuario, onGuardar, onCerrar }) {
  const [form, setForm] = useState({ nombre: '', correo: '', rol: '', scope: '' });
  const [error, setError] = useState('');

  useEffect(() => {
    if (usuario) {
      setForm({
        nombre: usuario.nombre,
        correo: usuario.correo,
        rol: usuario.rol,
        scope: usuario.scope,
      });
    } else {
      setForm({ nombre: '', correo: '', rol: '', scope: '' });
    }
  }, [usuario]);

  const campo = (key, value) => setForm((f) => ({ ...f, [key]: value }));

  const guardar = () => {
    if (!form.nombre.trim()) {
      setError('El nombre es obligatorio');
      return;
    }
    if (!form.correo.trim()) {
      setError('El correo es obligatorio');
      return;
    }
    if (!form.rol) {
      setError('El rol es obligatorio');
      return;
    }
    if (!form.scope) {
      setError('El scope es obligatorio');
      return;
    }
    setError('');
    onGuardar(form);
  };

  return (
    <Modal
      titulo={usuario ? 'Editar usuario' : 'Nuevo usuario'}
      onCerrar={onCerrar}
      ancho="max-w-lg"
      acciones={
        <div className="mt-5 flex justify-end gap-2">
          <Button variant="secondary" size="sm" onClick={onCerrar}>
            Cancelar
          </Button>
          <Button size="sm" onClick={guardar}>
            {usuario ? 'Guardar cambios' : 'Crear usuario'}
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
            placeholder="Ej: Juan García"
          />
        </div>
        <div>
          <label className="mb-1 block text-xs font-semibold text-gray-700">
            Correo electrónico
          </label>
          <Input
            value={form.correo}
            onChange={(e) => campo('correo', e.target.value)}
            placeholder="correo@avh.edu.co"
            disabled={!!usuario}
          />
        </div>
        <div>
          <label className="mb-1 block text-xs font-semibold text-gray-700">Rol</label>
          <Select value={form.rol} onChange={(e) => campo('rol', e.target.value)}>
            <option value="">Seleccionar rol</option>
            {ROLES.map((r) => (
              <option key={r} value={r}>
                {r}
              </option>
            ))}
          </Select>
        </div>
        <div>
          <label className="mb-1 block text-xs font-semibold text-gray-700">Scope</label>
          <Select value={form.scope} onChange={(e) => campo('scope', e.target.value)}>
            <option value="">Seleccionar scope</option>
            {SCOPES.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </Select>
        </div>
      </div>

      {!usuario && (
        <p className="rounded-lg bg-blue-50 px-3 py-2 text-xs text-gray-600">
          Se enviará una contraseña temporal al correo del usuario.
        </p>
      )}
    </Modal>
  );
}
