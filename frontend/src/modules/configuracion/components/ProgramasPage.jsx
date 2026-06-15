import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import configuracionService from '../services/configuracionService';
import { MOCK_PROGRAMAS, MOCK_FACULTADES } from '@/shared/mocks/datos';
import { ejecutarConsulta, placeholderSimple, usarMocks } from '@/shared/config/dataSource';
import TablaBase   from '../../../shared/components/TablaBase';
import BadgeEstado from '../../../shared/components/BadgeEstado';

export default function ProgramasPage({ esSubComponente = false }) {
  const [modal,      setModal]      = useState(false);
  const [editando,   setEditando]   = useState(null);
  const [nombre,     setNombre]     = useState('');
  const [facultadId, setFacultadId] = useState('');
  const [error,      setError]      = useState('');

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

  const abrirCrear  = () => { setEditando(null); setNombre(''); setFacultadId(''); setError(''); setModal(true); };
  const abrirEditar = (p) => { setEditando(p); setNombre(p.nombre); setFacultadId(p.facultad?.id ?? ''); setError(''); setModal(true); };
  const cerrar      = () => setModal(false);

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

  const crearMutation  = useMutation({ mutationFn: (dto)         => configuracionService.crearPrograma(dto),      onSuccess: alExitoModal('Programa creado correctamente'),      onError: alError });
  const editarMutation = useMutation({ mutationFn: ({ id, dto }) => configuracionService.editarPrograma(id, dto), onSuccess: alExitoModal('Programa actualizado correctamente'), onError: alError });
  const inactivarMutation = useMutation({
    mutationFn: (id) => configuracionService.inactivarPrograma(id),
    onSuccess: () => { queryClient.invalidateQueries(['programas']); toast.success('Programa inactivado'); },
    onError: alError,
  });

  const guardando = crearMutation.isPending || editarMutation.isPending;

  const guardar = () => {
    if (!nombre.trim())  { setError('El nombre es obligatorio');   return; }
    if (!facultadId)     { setError('La facultad es obligatoria'); return; }
    const dto = { nombre: nombre.trim(), facultad: { id: Number(facultadId) } };
    if (editando) {
      editarMutation.mutate({ id: editando.id, dto });
    } else {
      crearMutation.mutate(dto);
    }
  };

  const columnas = [
    { key: 'nombre',   titulo: 'Programa' },
    { key: 'facultad', titulo: 'Facultad', render: p => p.facultad?.nombre ?? p.facultad ?? '—' },
    { key: 'activo',   titulo: 'Estado', render: p => <BadgeEstado activo={p.activo} /> },
    {
      key: 'acciones', titulo: 'Acciones',
      render: p => (
        <div style={{ display: 'flex', gap: 6 }}>
          <button onClick={() => abrirEditar(p)} style={btn('#dbeafe', '#1e40af')}>Editar</button>
          <button
            disabled={inactivarMutation.isPending}
            onClick={() => inactivarMutation.mutate(p.id)}
            style={btn('#fee2e2', '#991b1b')}
          >
            Inactivar
          </button>
        </div>
      )
    }
  ];

  return (
    <div style={esSubComponente ? {} : { fontFamily: 'Arial, sans-serif' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
        {!esSubComponente && <h2 style={{ fontSize: 20, fontWeight: 700, color: '#1e3a5f', margin: 0 }}>Programas</h2>}
        <button onClick={abrirCrear}
          style={{ padding: '9px 18px', background: '#1e3a5f', color: '#fff', border: 'none', borderRadius: 8, cursor: 'pointer', fontSize: 13, fontWeight: 600, marginLeft: esSubComponente ? 'auto' : 0 }}>
          + Nuevo programa
        </button>
      </div>

      <TablaBase columnas={columnas} datos={programas} cargando={isLoading} />

      {modal && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 200 }}>
          <div style={{ background: '#fff', borderRadius: 12, padding: 28, width: 400, display: 'flex', flexDirection: 'column', gap: 12 }}>
            <h3 style={{ fontSize: 16, fontWeight: 700, color: '#1e3a5f', margin: 0 }}>
              {editando ? 'Editar programa' : 'Nuevo programa'}
            </h3>

            <label style={estiloLabel}>Nombre</label>
            <input
              value={nombre}
              onChange={e => setNombre(e.target.value)}
              style={estiloInput}
              placeholder="Ej: Ingeniería de Sistemas"
            />

            <label style={estiloLabel}>Facultad</label>
            <select
              value={facultadId}
              onChange={e => setFacultadId(e.target.value)}
              style={estiloInput}
            >
              <option value="">— Selecciona una facultad —</option>
              {facultades.map(f => (
                <option key={f.id} value={f.id}>{f.nombre}</option>
              ))}
            </select>

            {error && <p style={{ color: '#dc2626', fontSize: 12, margin: 0 }}>{error}</p>}

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
              <button onClick={cerrar}  disabled={guardando} style={{ padding: '8px 16px', background: '#f3f4f6', color: '#374151', border: 'none', borderRadius: 8, cursor: 'pointer' }}>
                Cancelar
              </button>
              <button onClick={guardar} disabled={guardando} style={{ padding: '8px 16px', background: '#1e3a5f', color: '#fff', border: 'none', borderRadius: 8, cursor: 'pointer' }}>
                {guardando ? 'Guardando...' : 'Guardar'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

const btn = (bg, color) => ({
  padding: '5px 12px', background: bg, color,
  border: 'none', borderRadius: 6, cursor: 'pointer', fontSize: 12, fontWeight: 500,
});

const estiloLabel = { fontSize: 13, fontWeight: 600, color: '#374151' };
const estiloInput = { padding: '9px 12px', border: '1px solid #d1d5db', borderRadius: 8, fontSize: 14 };