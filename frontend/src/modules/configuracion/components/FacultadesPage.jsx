import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import configuracionService from '../services/configuracionService';
import { MOCK_FACULTADES } from '@/shared/mocks/datos';
import { ejecutarConsulta, placeholderSimple, usarMocks } from '@/shared/config/dataSource';
import TablaBase   from '../../../shared/components/TablaBase';
import BadgeEstado from '../../../shared/components/BadgeEstado';

export default function FacultadesPage() {
  const [modal,    setModal]      = useState(false);
  const [editando, setEditando]   = useState(null);
  const [nombre,   setNombre]     = useState('');
  const [error,    setError]      = useState('');

  const { data: facultades = [], isLoading } = useQuery({
    queryKey: ['facultades', usarMocks()],
    queryFn: () =>
      ejecutarConsulta({
        mock: () => MOCK_FACULTADES,
        api: () => configuracionService.listarFacultades().then((r) => r.data.data ?? []),
      }),
    placeholderData: placeholderSimple(MOCK_FACULTADES),
  });

  const abrirCrear = () => { setEditando(null); setNombre(''); setError(''); setModal(true); };
  const abrirEditar = (f) => { setEditando(f); setNombre(f.nombre); setError(''); setModal(true); };
  const cerrar = () => setModal(false);

  const guardar = () => {
    if (!nombre.trim()) { setError('El nombre es obligatorio'); return; }
    // Aquí irá la llamada real al backend
    console.log(editando ? 'Editar:' : 'Crear:', nombre);
    cerrar();
  };

  const columnas = [
    { key: 'nombre', titulo: 'Nombre' },
    { key: 'activo', titulo: 'Estado', render: f => <BadgeEstado activo={f.activo} /> },
    {
      key: 'acciones', titulo: 'Acciones',
      render: f => (
        <div style={{ display: 'flex', gap: 6 }}>
          <button onClick={() => abrirEditar(f)} style={btn('#dbeafe','#1e40af')}>Editar</button>
          <button
            onClick={() => console.log(f.activo ? 'Inactivar' : 'Activar', f.id)}
            style={btn(f.activo ? '#fee2e2' : '#d1fae5', f.activo ? '#991b1b' : '#065f46')}
          >
            {f.activo ? 'Inactivar' : 'Activar'}
          </button>
        </div>
      )
    }
  ];

  return (
    <div style={{ fontFamily: 'Arial, sans-serif' }}>
      <div style={estilos.encabezado}>
        <h2 style={estilos.titulo}>Facultades</h2>
        <button onClick={abrirCrear} style={estilos.btnPrimario}>+ Nueva facultad</button>
      </div>

      <TablaBase columnas={columnas} datos={facultades} cargando={isLoading} />

      {modal && (
        <div style={estilos.overlay}>
          <div style={estilos.modal}>
            <h3 style={estilos.modalTitulo}>{editando ? 'Editar facultad' : 'Nueva facultad'}</h3>
            <label style={estilos.label}>Nombre</label>
            <input
              value={nombre}
              onChange={e => setNombre(e.target.value)}
              style={estilos.input}
              placeholder="Ej: Facultad de Ingeniería"
            />
            {error && <p style={{ color: '#dc2626', fontSize: 12 }}>{error}</p>}
            <div style={estilos.modalBtns}>
              <button onClick={cerrar}   style={btn('#f3f4f6','#374151')}>Cancelar</button>
              <button onClick={guardar}  style={estilos.btnPrimario}>Guardar</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

const btn = (bg, color) => ({
  padding: '5px 12px', background: bg, color,
  border: 'none', borderRadius: 6, cursor: 'pointer', fontSize: 12, fontWeight: 500
});

const estilos = {
  encabezado:   { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 },
  titulo:       { fontSize: 20, fontWeight: 700, color: '#1e3a5f', margin: 0 },
  btnPrimario:  { padding: '9px 18px', background: '#1e3a5f', color: '#fff', border: 'none', borderRadius: 8, cursor: 'pointer', fontSize: 13, fontWeight: 600 },
  overlay:      { position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 200 },
  modal:        { background: '#fff', borderRadius: 12, padding: 28, width: 400, display: 'flex', flexDirection: 'column', gap: 12 },
  modalTitulo:  { fontSize: 16, fontWeight: 700, color: '#1e3a5f', margin: 0 },
  modalBtns:    { display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 4 },
  label:        { fontSize: 13, fontWeight: 600, color: '#374151' },
  input:        { padding: '9px 12px', border: '1px solid #d1d5db', borderRadius: 8, fontSize: 14 }
};