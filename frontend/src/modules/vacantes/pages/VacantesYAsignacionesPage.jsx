import { useState } from 'react';
import VacantesPage    from './VacantesPage';
import AsignacionesPage from '../../asignaciones/pages/AsignacionesPage';

const TABS = [
  { id: 'vacantes',     label: 'Vacantes' },
  { id: 'asignaciones', label: 'Asignaciones' },
];

export default function VacantesYAsignacionesPage() {
  const [tabActiva, setTabActiva] = useState('vacantes');

  return (
    <div>
      <div style={estilos.tabBar}>
        {TABS.map((tab) => (
          <button
            key={tab.id}
            onClick={() => setTabActiva(tab.id)}
            style={tabActiva === tab.id ? { ...estilos.tab, ...estilos.tabActiva } : estilos.tab}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {tabActiva === 'vacantes'     && <VacantesPage />}
      {tabActiva === 'asignaciones' && <AsignacionesPage />}
    </div>
  );
}

const estilos = {
  tabBar: {
    display: 'flex',
    gap: 4,
    padding: '12px 20px 0',
    borderBottom: '2px solid #e5e7eb',
    background: '#fff',
  },
  tab: {
    padding: '8px 20px',
    border: 'none',
    borderBottom: '2px solid transparent',
    background: 'transparent',
    fontSize: 14,
    fontWeight: 500,
    color: '#6b7280',
    cursor: 'pointer',
    marginBottom: -2,
    fontFamily: 'Arial, sans-serif',
  },
  tabActiva: {
    color: '#1e3a5f',
    fontWeight: 700,
    borderBottom: '2px solid #1e3a5f',
  },
};
