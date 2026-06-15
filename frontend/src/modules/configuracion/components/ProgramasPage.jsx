import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import configuracionService from '../services/configuracionService';
import { MOCK_PROGRAMAS, MOCK_FACULTADES } from '@/shared/mocks/datos';
import { ejecutarConsulta, placeholderSimple, usarMocks } from '@/shared/config/dataSource';
import TablaBase from '../../../shared/components/TablaBase';
import BadgeEstado from '../../../shared/components/BadgeEstado';
import { Button, Input, Modal, PageHeader, Select } from '@/shared/components/ui';

export default function ProgramasPage() {
  const [modal, setModal] = useState(false);
  const [editando, setEditando] = useState(null);
  const [nombre, setNombre] = useState('');
  const [facultadId, setFacultadId] = useState('');
  const [error, setError] = useState('');

  const queryClient = useQueryClient();

  const { data: programas = [], isLoading } = useQuery({
    queryKey: ['programas', usarMocks()],
    queryFn: () =>
      ejecutarConsulta({
        mock: () => MOCK_PROGRAMAS,
        api: () => configuracionService.listarProgramas().then((r) => r.data ?? []),
      }),
    placeholderData: placeholderSimple(MOCK_PROGRAMAS),
  });

  const { data: facultades = [] } = useQuery({
    queryKey: ['facultades', usarMocks()],
    queryFn: () =>
      ejecutarConsulta({
        mock: () => MOCK_FACULTADES,
        api: () => configuracionService.listarFacultades().then((r) => r.data ?? []),
      }),
  });

  const abrirCrear = () => {
    setEditando(null);
    setNombre('');
    setFacultadId('');
    setError('');
    setModal(true);
  };
  const abrirEditar = (p) => {
    setEditando(p);
    setNombre(p.nombre);
    setFacultadId(p.facultad?.id ?? '');
    setError('');
    setModal(true);
  };
  const cerrar = () => setModal(false);

  const alExitoModal = (mensaje) => () => {
    queryClient.invalidateQueries(['programas']);
    toast.success(mensaje);
    cerrar();
  };

  const alError = (err) => {
    const msg =
      err?.response?.data?.mensaje ||
      err?.response?.data?.message ||
      'Error inesperado. Intenta de nuevo.';
    toast.error(msg);
  };

  const crearMutation = useMutation({
    mutationFn: (dto) => configuracionService.crearPrograma(dto),
    onSuccess: alExitoModal('Programa creado correctamente'),
    onError: alError,
  });
  const editarMutation = useMutation({
    mutationFn: ({ id, dto }) => configuracionService.editarPrograma(id, dto),
    onSuccess: alExitoModal('Programa actualizado correctamente'),
    onError: alError,
  });
  const inactivarMutation = useMutation({
    mutationFn: (id) => configuracionService.inactivarPrograma(id),
    onSuccess: () => {
      queryClient.invalidateQueries(['programas']);
      toast.success('Programa inactivado');
    },
    onError: alError,
  });

  const guardando = crearMutation.isPending || editarMutation.isPending;

  const guardar = () => {
    if (!nombre.trim()) {
      setError('El nombre es obligatorio');
      return;
    }
    if (!facultadId) {
      setError('La facultad es obligatoria');
      return;
    }
    const dto = { nombre: nombre.trim(), facultad: { id: Number(facultadId) } };
    if (editando) {
      editarMutation.mutate({ id: editando.id, dto });
    } else {
      crearMutation.mutate(dto);
    }
  };

  const columnas = [
    { key: 'nombre', titulo: 'Programa' },
    { key: 'facultad', titulo: 'Facultad', render: (p) => p.facultad?.nombre ?? p.facultad ?? '—' },
    { key: 'activo', titulo: 'Estado', render: (p) => <BadgeEstado activo={p.activo} /> },
    {
      key: 'acciones',
      titulo: 'Acciones',
      render: (p) => (
        <div className="flex flex-wrap gap-1.5">
          <Button variant="info" size="sm" onClick={() => abrirEditar(p)}>
            Editar
          </Button>
          <Button
            variant="danger"
            size="sm"
            disabled={inactivarMutation.isPending}
            onClick={() => inactivarMutation.mutate(p.id)}
          >
            Inactivar
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div>
      <PageHeader
        titulo="Programas"
        acciones={<Button onClick={abrirCrear}>+ Nuevo programa</Button>}
      />

      <TablaBase columnas={columnas} datos={programas} cargando={isLoading} />

      {modal && (
        <Modal
          titulo={editando ? 'Editar programa' : 'Nuevo programa'}
          onCerrar={cerrar}
          acciones={
            <div className="mt-5 flex justify-end gap-2">
              <Button variant="ghost" size="sm" onClick={cerrar} disabled={guardando}>
                Cancelar
              </Button>
              <Button size="sm" onClick={guardar} disabled={guardando}>
                {guardando ? 'Guardando...' : 'Guardar'}
              </Button>
            </div>
          }
        >
          <label htmlFor="nombre-programa" className="block text-sm font-medium text-gray-700">
            Nombre
          </label>
          <Input
            id="nombre-programa"
            value={nombre}
            onChange={(e) => setNombre(e.target.value)}
            placeholder="Ej: Ingeniería de Sistemas"
          />

          <label htmlFor="facultad-programa" className="mt-3 block text-sm font-medium text-gray-700">
            Facultad
          </label>
          <Select
            id="facultad-programa"
            value={facultadId}
            onChange={(e) => setFacultadId(e.target.value)}
          >
            <option value="">— Selecciona una facultad —</option>
            {facultades.map((f) => (
              <option key={f.id} value={f.id}>
                {f.nombre}
              </option>
            ))}
          </Select>

          {error && <p className="text-sm text-red-600">{error}</p>}
        </Modal>
      )}
    </div>
  );
}
