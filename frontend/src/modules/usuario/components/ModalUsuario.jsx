import { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import configuracionService from '../../configuracion/services/configuracionService';
import empresaService from '../../empresa/services/empresaService';
import { MOCK_FACULTADES } from '@/shared/mocks/datos';
import { ejecutarConsulta, usarMocks } from '@/shared/config/dataSource';
import { Button, Input, Modal, Select } from '@/shared/components/ui';
import { dtoUsuario, opcionesRol, requiereEmpresa, requiereFacultad } from '../constants/catalogoUsuario';

const FORM_VACIO = {
  nombre: '',
  correo: '',
  rol: '',
  facultadId: '',
  empresaId: '',
  cargoTutor: '',
  telefonoTutor: '',
};

export default function ModalUsuario({ usuario, onGuardar, onCerrar }) {
  const [form, setForm] = useState(FORM_VACIO);
  const [error, setError] = useState('');

  useEffect(() => {
    if (usuario) {
      setForm({
        nombre: usuario.nombre,
        correo: usuario.correo,
        rol: usuario.rol,
        facultadId: usuario.facultadId ? String(usuario.facultadId) : '',
        empresaId: usuario.empresaId ? String(usuario.empresaId) : '',
        cargoTutor: usuario.cargoTutor ?? '',
        telefonoTutor: usuario.telefonoTutor ?? '',
      });
    } else {
      setForm(FORM_VACIO);
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

  const { data: empresas = [] } = useQuery({
    queryKey: ['empresas-select-usuario'],
    queryFn: () =>
      empresaService.listar({ page: 0, size: 500, activo: true }).then((r) => r.data?.content ?? []),
  });

  const campo = (key, value) => setForm((f) => ({ ...f, [key]: value }));

  const cambiarRol = (rol) => {
    setForm((f) => ({
      ...f,
      rol,
      facultadId: requiereFacultad(rol) ? f.facultadId : '',
      empresaId: requiereEmpresa(rol) ? f.empresaId : '',
      cargoTutor: requiereEmpresa(rol) ? f.cargoTutor : '',
      telefonoTutor: requiereEmpresa(rol) ? f.telefonoTutor : '',
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
    if (requiereEmpresa(form.rol)) {
      if (!form.empresaId) {
        setError('Debe seleccionar la empresa para el tutor empresarial');
        return;
      }
      if (!form.telefonoTutor.trim()) {
        setError('El teléfono es obligatorio para el tutor empresarial');
        return;
      }
    }
    setError('');
    onGuardar(dtoUsuario(form));
  };

  const mostrarFacultad = requiereFacultad(form.rol);
  const mostrarEmpresa = requiereEmpresa(form.rol);

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
            placeholder="correo@empresa.com"
            disabled={!!usuario}
          />
        </div>
        <div className={mostrarFacultad || mostrarEmpresa ? '' : 'sm:col-span-2'}>
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
        {mostrarFacultad && (
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
        {mostrarEmpresa && (
          <>
            <div className="sm:col-span-2">
              <label className="mb-1 block text-xs font-semibold text-gray-700">Empresa</label>
              <Select
                value={form.empresaId}
                onChange={(e) => campo('empresaId', e.target.value)}
              >
                <option value="">Seleccionar empresa</option>
                {empresas.map((e) => (
                  <option key={e.id} value={e.id}>
                    {e.razonSocial ?? e.nombre ?? `Empresa ${e.id}`}
                  </option>
                ))}
              </Select>
              <p className="mt-1 text-xs text-gray-500">
                El tutor quedará vinculado a esta empresa en el sistema.
              </p>
            </div>
            <div>
              <label className="mb-1 block text-xs font-semibold text-gray-700">Cargo en la empresa</label>
              <Input
                value={form.cargoTutor}
                onChange={(e) => campo('cargoTutor', e.target.value)}
                placeholder="Ej: Jefe de recursos humanos"
              />
            </div>
            <div>
              <label className="mb-1 block text-xs font-semibold text-gray-700">Teléfono</label>
              <Input
                value={form.telefonoTutor}
                onChange={(e) => campo('telefonoTutor', e.target.value)}
                placeholder="Ej: 3001234567"
              />
            </div>
          </>
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
