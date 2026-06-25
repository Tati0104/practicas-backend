import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import configuracionService from '../services/configuracionService';
import { MOCK_CATALOGO_PRACTICAS, MOCK_PROGRAMAS } from '@/shared/mocks/datos';
import { ejecutarConsulta, placeholderSimple, usarMocks } from '@/shared/config/dataSource';
import TablaBase from '../../../shared/components/TablaBase';
import BadgeEstado from '../../../shared/components/BadgeEstado';
import { Button, Input, Modal, PageHeader, Select } from '@/shared/components/ui';

const FORM_VACIO = {
  programaId: '',
  numeroPractica: '',
  nombre: '',
  materiaNucleo: '',
  codigoMateria: '',
  numCortes: '',
  duracionSemanas: '',
};

export default function CatalogoPracticasPage() {
  const [modal, setModal] = useState(false);
  const [editando, setEditando] = useState(null);
  const [form, setForm] = useState(FORM_VACIO);
  const [error, setError] = useState('');
  const [filtroProgramaId, setFiltroProgramaId] = useState('');

  const queryClient = useQueryClient();
  const setField = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }));

  const { data: programas = [] } = useQuery({
    queryKey: ['programas', usarMocks()],
    queryFn: () =>
      ejecutarConsulta({
        mock: () => MOCK_PROGRAMAS,
        api: () => configuracionService.listarProgramas().then((r) => r.data ?? []),
      }),
  });

  const { data: catalogo = [], isLoading } = useQuery({
    queryKey: ['catalogo-practicas', filtroProgramaId, usarMocks()],
    queryFn: () =>
      ejecutarConsulta({
        mock: () =>
          filtroProgramaId
            ? MOCK_CATALOGO_PRACTICAS.filter((c) => c.programa?.id === Number(filtroProgramaId))
            : MOCK_CATALOGO_PRACTICAS,
        api: () =>
          configuracionService
            .listarCatalogoPracticas(filtroProgramaId || undefined)
            .then((r) => r.data ?? []),
      }),
    placeholderData: placeholderSimple(MOCK_CATALOGO_PRACTICAS),
  });

  const abrirCrear = () => {
    setEditando(null);
    setForm(FORM_VACIO);
    setError('');
    setModal(true);
  };

  const abrirEditar = (c) => {
    setEditando(c);
    setForm({
      programaId: c.programa?.id ?? '',
      numeroPractica: c.numeroPractica ?? '',
      nombre: c.nombre ?? '',
      materiaNucleo: c.materiaNucleo ?? '',
      codigoMateria: c.codigoMateria ?? '',
      numCortes: c.numCortes ?? '',
      duracionSemanas: c.duracionSemanas ?? '',
    });
    setError('');
    setModal(true);
  };

  const cerrar = () => setModal(false);

  const alExitoModal = (mensaje) => () => {
    queryClient.invalidateQueries(['catalogo-practicas']);
    toast.success(mensaje);
    cerrar();
  };

  const alExito = (mensaje) => () => {
    queryClient.invalidateQueries(['catalogo-practicas']);
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
    mutationFn: (dto) => configuracionService.crearCatalogoPractica(dto),
    onSuccess: alExitoModal('Práctica creada correctamente'),
    onError: alError,
  });

  const editarMutation = useMutation({
    mutationFn: ({ id, dto }) => configuracionService.editarCatalogoPractica(id, dto),
    onSuccess: alExitoModal('Práctica actualizada correctamente'),
    onError: alError,
  });

  const activarMutation = useMutation({
    mutationFn: (id) => configuracionService.activarCatalogoPractica(id),
    onSuccess: alExito('Práctica activada'),
    onError: alError,
  });

  const desactivarMutation = useMutation({
    mutationFn: (id) => configuracionService.desactivarCatalogoPractica(id),
    onSuccess: alExito('Práctica desactivada'),
    onError: alError,
  });

  const guardando = crearMutation.isPending || editarMutation.isPending;

  const guardar = () => {
    if (!form.programaId) { setError('El programa es obligatorio'); return; }
    if (!form.numeroPractica || Number(form.numeroPractica) < 1) { setError('El nivel de práctica debe ser mayor a 0'); return; }
    if (!form.nombre.trim()) { setError('El nombre es obligatorio'); return; }
    if (!form.materiaNucleo.trim()) { setError('La materia núcleo es obligatoria'); return; }
    if (!form.codigoMateria.trim()) { setError('El código de materia es obligatorio'); return; }
    if (!form.numCortes || Number(form.numCortes) < 1) { setError('El número de cortes debe ser mayor a 0'); return; }
    if (!form.duracionSemanas || Number(form.duracionSemanas) < 1) { setError('La duración debe ser mayor a 0'); return; }

    const dto = {
      programa: { id: Number(form.programaId) },
      numeroPractica: Number(form.numeroPractica),
      nombre: form.nombre.trim(),
      materiaNucleo: form.materiaNucleo.trim(),
      codigoMateria: form.codigoMateria.trim(),
      numCortes: Number(form.numCortes),
      duracionSemanas: Number(form.duracionSemanas),
    };

    if (editando) {
      editarMutation.mutate({ id: editando.id, dto });
    } else {
      crearMutation.mutate(dto);
    }
  };

  const columnas = [
    {
      key: 'numeroPractica',
      titulo: 'Nivel',
      render: (c) => (
        <span className="font-semibold text-gray-700">Práctica {c.numeroPractica}</span>
      ),
    },
    { key: 'nombre', titulo: 'Nombre' },
    { key: 'materiaNucleo', titulo: 'Materia núcleo' },
    { key: 'codigoMateria', titulo: 'Código' },
    {
      key: 'programa',
      titulo: 'Programa',
      render: (c) => c.programa?.nombre ?? '—',
    },
    {
      key: 'numCortes',
      titulo: 'Cortes',
      render: (c) => c.numCortes,
    },
    {
      key: 'duracionSemanas',
      titulo: 'Duración',
      render: (c) => `${c.duracionSemanas} sem.`,
    },
    {
      key: 'activo',
      titulo: 'Estado',
      render: (c) => <BadgeEstado activo={c.activo} />,
    },
    {
      key: 'acciones',
      titulo: 'Acciones',
      render: (c) => (
        <div className="flex flex-wrap gap-1.5">
          <Button variant="info" size="sm" onClick={() => abrirEditar(c)}>
            Editar
          </Button>
          <Button
            variant={c.activo ? 'danger' : 'success'}
            size="sm"
            disabled={activarMutation.isPending || desactivarMutation.isPending}
            onClick={() =>
              c.activo ? desactivarMutation.mutate(c.id) : activarMutation.mutate(c.id)
            }
          >
            {c.activo ? 'Desactivar' : 'Activar'}
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div>
      <PageHeader
        titulo="Catálogo de prácticas"
        descripcion="Plantillas de práctica por programa. Definen la materia núcleo y duración de cada nivel."
        acciones={<Button onClick={abrirCrear}>+ Nueva práctica</Button>}
      />

      <div className="mb-4 flex items-center gap-3">
        <label htmlFor="filtro-programa" className="text-sm font-medium text-gray-700 whitespace-nowrap">
          Filtrar por programa:
        </label>
        <Select
          id="filtro-programa"
          value={filtroProgramaId}
          onChange={(e) => setFiltroProgramaId(e.target.value)}
          className="max-w-xs"
        >
          <option value="">Todos los programas</option>
          {programas.map((p) => (
            <option key={p.id} value={p.id}>
              {p.nombre}
            </option>
          ))}
        </Select>
      </div>

      <TablaBase columnas={columnas} datos={catalogo} cargando={isLoading} />

      {modal && (
        <Modal
          titulo={editando ? 'Editar práctica' : 'Nueva práctica'}
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
          <div className="space-y-3">
            <div>
              <label htmlFor="cp-programa" className="block text-sm font-medium text-gray-700">
                Programa
              </label>
              <Select id="cp-programa" value={form.programaId} onChange={setField('programaId')}>
                <option value="">— Selecciona un programa —</option>
                {programas.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.nombre}
                  </option>
                ))}
              </Select>
            </div>

            <div>
              <label htmlFor="cp-nivel" className="block text-sm font-medium text-gray-700">
                Nivel de práctica
              </label>
              <Input
                id="cp-nivel"
                type="number"
                min="1"
                value={form.numeroPractica}
                onChange={setField('numeroPractica')}
                placeholder="Ej: 1"
              />
            </div>

            <div>
              <label htmlFor="cp-nombre" className="block text-sm font-medium text-gray-700">
                Nombre
              </label>
              <Input
                id="cp-nombre"
                value={form.nombre}
                onChange={setField('nombre')}
                placeholder="Ej: Práctica I"
              />
            </div>

            <div>
              <label htmlFor="cp-materia" className="block text-sm font-medium text-gray-700">
                Materia núcleo
              </label>
              <Input
                id="cp-materia"
                value={form.materiaNucleo}
                onChange={setField('materiaNucleo')}
                placeholder="Ej: Ingeniería de Software"
              />
            </div>

            <div>
              <label htmlFor="cp-codigo" className="block text-sm font-medium text-gray-700">
                Código de materia
              </label>
              <Input
                id="cp-codigo"
                value={form.codigoMateria}
                onChange={setField('codigoMateria')}
                placeholder="Ej: INS001"
              />
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label htmlFor="cp-cortes" className="block text-sm font-medium text-gray-700">
                  Número de cortes
                </label>
                <Input
                  id="cp-cortes"
                  type="number"
                  min="1"
                  value={form.numCortes}
                  onChange={setField('numCortes')}
                  placeholder="Ej: 3"
                />
              </div>
              <div>
                <label htmlFor="cp-semanas" className="block text-sm font-medium text-gray-700">
                  Duración (semanas)
                </label>
                <Input
                  id="cp-semanas"
                  type="number"
                  min="1"
                  value={form.duracionSemanas}
                  onChange={setField('duracionSemanas')}
                  placeholder="Ej: 12"
                />
              </div>
            </div>

            {error && <p className="text-sm text-red-600">{error}</p>}
          </div>
        </Modal>
      )}
    </div>
  );
}