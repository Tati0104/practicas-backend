import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import configuracionService from '../services/configuracionService';
import { MOCK_FACULTADES } from '@/shared/mocks/datos';
import { ejecutarConsulta, placeholderSimple, usarMocks } from '@/shared/config/dataSource';
import TablaBase from '../../../shared/components/TablaBase';
import BadgeEstado from '../../../shared/components/BadgeEstado';
import { Button, Input, Modal, PageHeader } from '@/shared/components/ui';

export default function FacultadesPage({ esSubComponente = false }) {
  const [modal, setModal] = useState(false);
  const [editando, setEditando] = useState(null);
  const [nombre, setNombre] = useState('');
  const [error, setError] = useState('');

  const queryClient = useQueryClient();

  const { data: facultades = [], isLoading } = useQuery({
    queryKey: ['facultades', usarMocks()],
    queryFn: () =>
      ejecutarConsulta({
        mock: () => MOCK_FACULTADES,
        api: () => configuracionService.listarFacultades().then((r) => r.data ?? []),
      }),
    placeholderData: placeholderSimple(MOCK_FACULTADES),
  });

  const abrirCrear = () => {
    setEditando(null);
    setNombre('');
    setError('');
    setModal(true);
  };
  const abrirEditar = (f) => {
    setEditando(f);
    setNombre(f.nombre);
    setError('');
    setModal(true);
  };
  const cerrar = () => setModal(false);

  const alExitoModal = (mensaje) => () => {
    queryClient.invalidateQueries(['facultades']);
    toast.success(mensaje);
    cerrar();
  };

  const alExito = (mensaje) => () => {
    queryClient.invalidateQueries(['facultades']);
    toast.success(mensaje);
  };

  const alError = (err) => {
    const msg =
      err?.response?.data?.mensaje ||
      err?.response?.data?.message ||
      'Error inesperado. Intenta de nuevo.';
    toast.error(msg);
  };

  const crearMutation = useMutation({
    mutationFn: (dto) => configuracionService.crearFacultad(dto),
    onSuccess: alExitoModal('Facultad creada correctamente'),
    onError: alError,
  });
  const editarMutation = useMutation({
    mutationFn: ({ id, dto }) => configuracionService.editarFacultad(id, dto),
    onSuccess: alExitoModal('Facultad actualizada correctamente'),
    onError: alError,
  });
  const activarMutation = useMutation({
    mutationFn: (id) => configuracionService.activarFacultad(id),
    onSuccess: alExito('Facultad activada'),
    onError: alError,
  });
  const inactivarMutation = useMutation({
    mutationFn: (id) => configuracionService.inactivarFacultad(id),
    onSuccess: alExito('Facultad inactivada'),
    onError: alError,
  });

  const guardando = crearMutation.isPending || editarMutation.isPending;

  const guardar = () => {
    if (!nombre.trim()) {
      setError('El nombre es obligatorio');
      return;
    }
    if (editando) {
      editarMutation.mutate({ id: editando.id, dto: { nombre: nombre.trim() } });
    } else {
      crearMutation.mutate({ nombre: nombre.trim() });
    }
  };

  const columnas = [
    { key: 'nombre', titulo: 'Nombre' },
    { key: 'activo', titulo: 'Estado', render: (f) => <BadgeEstado activo={f.activo} /> },
    {
      key: 'acciones',
      titulo: 'Acciones',
      render: (f) => (
        <div className="flex flex-wrap gap-1.5">
          <Button variant="info" size="sm" onClick={() => abrirEditar(f)}>
            Editar
          </Button>
          <Button
            variant={f.activo ? 'danger' : 'success'}
            size="sm"
            disabled={activarMutation.isPending || inactivarMutation.isPending}
            onClick={() => (f.activo ? inactivarMutation.mutate(f.id) : activarMutation.mutate(f.id))}
          >
            {f.activo ? 'Inactivar' : 'Activar'}
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div>
      {!esSubComponente ? (
        <PageHeader
          titulo="Facultades"
          acciones={<Button onClick={abrirCrear}>+ Nueva facultad</Button>}
        />
      ) : (
        <div className="mb-4 flex justify-end">
          <Button onClick={abrirCrear}>+ Nueva facultad</Button>
        </div>
      )}

      <TablaBase columnas={columnas} datos={facultades} cargando={isLoading} />

      {modal && (
        <Modal
          titulo={editando ? 'Editar facultad' : 'Nueva facultad'}
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
          <label htmlFor="nombre-facultad" className="block text-sm font-medium text-gray-700">
            Nombre
          </label>
          <Input
            id="nombre-facultad"
            value={nombre}
            onChange={(e) => setNombre(e.target.value)}
            placeholder="Ej: Facultad de Ingeniería"
          />
          {error && <p className="text-sm text-red-600">{error}</p>}
        </Modal>
      )}
    </div>
  );
}
