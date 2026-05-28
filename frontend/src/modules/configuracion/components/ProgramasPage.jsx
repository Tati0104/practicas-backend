import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import configuracionService from '../services/configuracionService';
import TablaBase   from '../../../shared/components/TablaBase';
import BadgeEstado from '../../../shared/components/BadgeEstado';

const MOCK_PROGRAMAS = [
  { id: 1, nombre: 'Ingeniería de Sistemas', facultad: 'Facultad de Ingeniería', activo: true },
  { id: 2, nombre: 'Ingeniería Civil',        facultad: 'Facultad de Ingeniería', activo: true },
  { id: 3, nombre: 'Administración',          facultad: 'Facultad de Económicas', activo: false },
];

export default function ProgramasPage() {
  const [modal,  setModal]  = useState(false);
  const [nombre, setNombre] = useState('');
  const [error,  setError]  = useState('');

  const { data: programas = MOCK_PROGRAMAS, isLoading } = useQuery({
    queryKey:    ['programas'],
    queryFn:     () => configuracionService.listarProgramas().then(r => r.data.data),
    initialData: MOCK_PROGRAMAS
  });

  const guardar = () => {
    if (!nombre.trim()) { setError('El nombre es obligatorio'); return; }
    console.log('Crear programa:', nombre);
    setModal(false);
  };

  const columnas = [
    { key: 'nombre',   titulo: 'Programa' },
    { key: 'facultad', titulo: 'Facultad' },
    { key: 'activo',   titulo: 'Estado', render: p => <BadgeEstado activo={p.activo} /> },
    {
      key: 'acciones', titulo: 'Acciones',
      render: p => (
        <button
          onClick={() => console.log('Editar', p.id)}
          style={{ padding: '5px 12px', background: '#dbeafe', color: '#1e40af', border: 'none', borderRadius: 6, cursor: 'pointer', fontSize: 12 }}
        >
          Editar
        </button>
      )
    }
  ];

  return (
    <div style={{ fontFamily: 'Arial, sans-serif' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
        <h2 style={{ fontSize: 20, fontWeight: 700, color: '#1e3a5f', margin: 0 }}>Programas</h2>
        <button onClick={() => { setNombre(''); setError(''); setModal(true); }}
          style={{ padding: '9px 18px', background: '#1e3a5f', color: '#fff', border: 'none', borderRadius: 8, cursor: 'pointer', fontSize: 13, fontWeight: 600 }}>
          + Nuevo programa
        </button>
      </div>

      <TablaBase columnas={columnas} datos={programas} cargando={isLoading} />

      {modal && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 200 }}>
          <div style={{ background: '#fff', borderRadius: 12, padding: 28, width: 400, display: 'flex', flexDirection: 'column', gap: 12 }}>
            <h3 style={{ fontSize: 16, fontWeight: 700, color: '#1e3a5f', margin: 0 }}>Nuevo programa</h3>
            <label style={{ fontSize: 13, fontWeight: 600, color: '#374151' }}>Nombre</label>
            <input value={nombre} onChange={e => setNombre(e.target.value)}
              style={{ padding: '9px 12px', border: '1px solid #d1d5db', borderRadius: 8, fontSize: 14 }}
              placeholder="Ej: Ingeniería de Sistemas" />
            {error && <p style={{ color: '#dc2626', fontSize: 12, margin: 0 }}>{error}</p>}
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
              <button onClick={() => setModal(false)} style={{ padding: '8px 16px', background: '#f3f4f6', color: '#374151', border: 'none', borderRadius: 8, cursor: 'pointer' }}>Cancelar</button>
              <button onClick={guardar} style={{ padding: '8px 16px', background: '#1e3a5f', color: '#fff', border: 'none', borderRadius: 8, cursor: 'pointer' }}>Guardar</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}