import { useState } from 'react';
import FacultadesPage from './FacultadesPage';
import ProgramasPage from './ProgramasPage';

export default function FacultadesProgramasPage() {
  const [tab, setTab] = useState('facultades');

  return (
    <div style={{ padding: 20, fontFamily: 'Arial, sans-serif' }}>
      <h1 style={{ fontSize: 22, fontWeight: 800, color: '#111827', marginBottom: 20, marginTop: 0 }}>
        Facultades y Programas
      </h1>
      
      <div style={{ display: 'flex', gap: 20, borderBottom: '1px solid #e5e7eb', marginBottom: 20 }}>
        <button
          onClick={() => setTab('facultades')}
          style={{
            padding: '10px 4px',
            background: 'none',
            border: 'none',
            borderBottom: tab === 'facultades' ? '2px solid #2563eb' : '2px solid transparent',
            color: tab === 'facultades' ? '#2563eb' : '#6b7280',
            fontWeight: 600,
            cursor: 'pointer',
            fontSize: 14,
            marginBottom: -1
          }}
        >
          Facultades
        </button>
        <button
          onClick={() => setTab('programas')}
          style={{
            padding: '10px 4px',
            background: 'none',
            border: 'none',
            borderBottom: tab === 'programas' ? '2px solid #2563eb' : '2px solid transparent',
            color: tab === 'programas' ? '#2563eb' : '#6b7280',
            fontWeight: 600,
            cursor: 'pointer',
            fontSize: 14,
            marginBottom: -1
          }}
        >
          Programas
        </button>
      </div>

      <div style={{ background: '#fff', border: '1px solid #e5e7eb', borderRadius: 10, padding: 20 }}>
        {tab === 'facultades' ? <FacultadesPage esSubComponente={true} /> : <ProgramasPage esSubComponente={true} />}
      </div>
    </div>
  );
}
