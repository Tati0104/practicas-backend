import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import docentesAsesoresService from '../services/docentesAsesoresService';
import configuracionService from '../../configuracion/services/configuracionService';
import TablaBase from '../../../shared/components/TablaBase';
import BadgeEstado from '../../../shared/components/BadgeEstado';
import { Button, Input, Modal, PageHeader } from '@/shared/components/ui';

export default function DocentesAsesoresPage() {
  const [modal, setModal] = useState(false);
  const [editando, setEditando] = useState(null);
  
  // Form fields
  const [nombreCompleto, setNombreCompleto] = useState('');
  const [correo, setCorreo] = useState('');
  const [telefono, setTelefono] = useState('');
  const [programaId, setProgramaId] = useState('');
  const [areaConocimiento, setAreaConocimiento] = useState('');
  const [error, setError] = useState('');

  const queryClient = useQueryClient();

  const { data: docentes = [], isLoading } = useQuery({
    queryKey: ['docentes-asesores'],
    queryFn: () => docentesAsesoresService.listarPorPrograma().then(res => res || []),
  });

  const { data: programas = [] } = useQuery({
    queryKey: ['programas-all'],
    queryFn: () => configuracionService.listarProgramas().then((r) => r.data ?? []),
  });

  const abrirCrear = () => {
    setEditando(null);
    setNombreCompleto('');
    setCorreo('');
    setTelefono('');
    setProgramaId('');
    setAreaConocimiento('');
    setError('');
    setModal(true);
  };

  const abrirEditar = (d) => {
    setEditando(d);
    setNombreCompleto(d.nombreCompleto || d.nombre);
    setCorreo(d.correo);
    setTelefono(d.telefono || '');
    setProgramaId(d.programaId || '');
    setAreaConocimiento(d.areaConocimiento || '');
    setError('');
    setModal(true);
  };

  const cerrar = () => setModal(false);

  const alExitoModal = (mensaje) => () => {
    queryClient.invalidateQueries(['docentes-asesores']);
    toast.success(mensaje);
    cerrar();
  };

  const alExito = (mensaje) => () => {
    queryClient.invalidateQueries(['docentes-asesores']);
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
    mutationFn: (dto) => docentesAsesoresService.crear(dto),
    onSuccess: alExitoModal('Docente asesor creado correctamente'),
    onError: alError,
  });

  const editarMutation = useMutation({
    mutationFn: ({ id, dto }) => docentesAsesoresService.actualizar(id, dto),
    onSuccess: alExitoModal('Docente asesor actualizado correctamente'),
    onError: alError,
  });

  const estadoMutation = useMutation({
    mutationFn: ({ id, activo }) => docentesAsesoresService.cambiarEstado(id, activo),
    onSuccess: (_, variables) => {
      alExito(variables.activo ? 'Docente activado' : 'Docente inactivado')();
    },
    onError: alError,
  });

  const guardando = crearMutation.isPending || editarMutation.isPending;

  const guardar = () => {
    if (!nombreCompleto.trim() || !correo.trim() || !programaId) {
      setError('El nombre, el correo y el programa son obligatorios');
      return;
    }
    
    const payload = { 
      nombreCompleto: nombreCompleto.trim(), 
      correo: correo.trim(),
      telefono: telefono.trim(),
      programaId: Number(programaId),
      areaConocimiento: areaConocimiento.trim()
    };

    if (editando) {
      editarMutation.mutate({ id: editando.id, dto: payload });
    } else {
      crearMutation.mutate(payload);
    }
  };

  const getNombrePrograma = (id) => {
    const p = programas.find(x => x.id === id);
    return p ? p.nombre : 'Desconocido';
  };

  const columnas = [
    { key: 'nombreCompleto', titulo: 'Nombre', render: (d) => d.nombreCompleto || d.nombre },
    { key: 'correo', titulo: 'Correo' },
    { key: 'telefono', titulo: 'Teléfono' },
    { key: 'programaId', titulo: 'Programa', render: (d) => getNombrePrograma(d.programaId) },
    { key: 'activo', titulo: 'Estado', render: (d) => <BadgeEstado activo={d.activo} /> },
    {
      key: 'acciones',
      titulo: 'Acciones',
      render: (d) => (
        <div className="flex flex-wrap gap-1.5">
          <Button variant="info" size="sm" onClick={() => abrirEditar(d)}>
            Editar
          </Button>
          <Button
            variant={d.activo ? 'danger' : 'success'}
            size="sm"
            disabled={estadoMutation.isPending}
            onClick={() => estadoMutation.mutate({ id: d.id, activo: !d.activo })}
          >
            {d.activo ? 'Inactivar' : 'Activar'}
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div className="p-6">
      <PageHeader
        titulo="Docentes Asesores"
        acciones={<Button onClick={abrirCrear}>+ Nuevo Docente</Button>}
      />

      <TablaBase columnas={columnas} datos={docentes} cargando={isLoading} />

      {modal && (
        <Modal
          titulo={editando ? 'Editar Docente Asesor' : 'Nuevo Docente Asesor'}
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
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">Nombre Completo</label>
              <Input
                value={nombreCompleto}
                onChange={(e) => setNombreCompleto(e.target.value)}
                placeholder="Ej: Juan Pérez"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Correo Electrónico</label>
              <Input
                type="email"
                value={correo}
                onChange={(e) => setCorreo(e.target.value)}
                placeholder="Ej: juan.perez@universidad.edu.co"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Teléfono</label>
              <Input
                value={telefono}
                onChange={(e) => setTelefono(e.target.value)}
                placeholder="Ej: 3001234567"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Programa Académico</label>
              <select
                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm p-2 border"
                value={programaId}
                onChange={(e) => setProgramaId(e.target.value)}
              >
                <option value="">Seleccione un programa...</option>
                {programas.map(p => (
                  <option key={p.id} value={p.id}>{p.nombre}</option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Área de Conocimiento (Opcional)</label>
              <Input
                value={areaConocimiento}
                onChange={(e) => setAreaConocimiento(e.target.value)}
                placeholder="Ej: Ingeniería de Software"
              />
            </div>
          </div>
          {error && <p className="mt-3 text-sm text-red-600">{error}</p>}
        </Modal>
      )}
    </div>
  );
}
