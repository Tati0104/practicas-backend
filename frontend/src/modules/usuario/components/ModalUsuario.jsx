import { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import configuracionService from '../../configuracion/services/configuracionService';
import { MOCK_FACULTADES } from '@/shared/mocks/datos';
import { ejecutarConsulta, usarMocks } from '@/shared/config/dataSource';
import { Button, Input, Modal, Select } from '@/shared/components/ui';
import { dtoUsuario, opcionesRol, requiereFacultad } from '../constants/catalogoUsuario';

export default function ModalUsuario({ usuario, onGuardar, onCerrar }) {
  const [form, setForm] = useState({ nombre: '', correo: '', rol: '', facultadId: '' });
  const [error, setError] = useState('');

  useEffect(() => {
    if (usuario) {
      setForm({
        nombre: usuario.nombre,
        correo: usuario.correo,
        rol: usuario.rol,
        facultadId: usuario.facultadId ? String(usuario.facultadId) : '',
      });
    } else {
      setForm({ nombre: '', correo: '', rol: '', facultadId: '' });
    }
  }, [usuario]);

  const { data: facultades = [] } = useQuery({
    queryKey: ['facultades', usarMocks()],
    queryFn: () =>
      ejecutarConsulta({
        mock: () => MOCK_FACULTADES,
        api: () => configuracionService.listarFacultades().then((r) => r.data ?? []),
      }),
  });

  const campo = (key, value) => setForm((f) => ({ ...f, [key]: value }));

  const cambiarRol = (rol) => {
    setForm((f) => ({
      ...f,
      rol,
      facultadId: requiereFacultad(rol) ? f.facultadId : '',
    }));
  };

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
    if (requiereFacultad(form.rol) && !form.facultadId) {
      setError('Debe seleccionar la facultad para este rol');
      return;
    }
    setError('');
    onGuardar(dtoUsuario(form));
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
        <div className={requiereFacultad(form.rol) ? '' : 'sm:col-span-2'}>
          <label className="mb-1 block text-xs font-semibold text-gray-700">Rol</label>
          <Select value={form.rol} onChange={(e) => cambiarRol(e.target.value)}>
            <option value="">Seleccionar rol</option>
            {opcionesRol().map((r) => (
              <option key={r.value} value={r.value}>
                {r.label}
              </option>
            ))}
          </Select>
        </div>
        {requiereFacultad(form.rol) && (
          <div>
            <label className="mb-1 block text-xs font-semibold text-gray-700">Facultad</label>
            <Select
              value={form.facultadId}
              onChange={(e) => campo('facultadId', e.target.value)}
            >
              <option value="">Seleccionar facultad</option>
              {facultades.map((f) => (
                <option key={f.id} value={f.id}>
                  {f.nombre}
                </option>
              ))}
            </Select>
            <p className="mt-1 text-xs text-gray-500">
              Verá y gestionará los programas y estudiantes de esta facultad.
            </p>
          </div>
        )}
      </div>

      {!usuario && (
        <p className="mt-3 rounded-lg bg-blue-50 px-3 py-2 text-xs text-gray-600">
          Se enviará una contraseña temporal al correo del usuario.
        </p>
      )}
    </Modal>
  );
}
