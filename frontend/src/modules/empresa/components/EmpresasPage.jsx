import { useState } from 'react';
import { useEmpresas } from '../hooks/useEmpresas';
import ModalEmpresa  from './ModalEmpresa';
import TablaBase     from '../../../shared/components/TablaBase';
import BadgeEstado   from '../../../shared/components/BadgeEstado';

export default function EmpresasPage() {
  const { empresas, isLoading, filtros, setFiltros,
          registrar, activar, inactivar } = useEmpresas();
  const [modal,    setModal]    = useState(false);
  const [busqueda, setBusqueda] = useState('');

  const empresasFiltradas = empresas.filter(e =>
    e.razonSocial.toLowerCase().includes(busqueda.toLowerCase()) ||
    e.nit.includes(busqueda)
  );

  const columnas = [
    { key: 'nit',        titulo: 'NIT' },
    { key: 'razonSocial',titulo: 'Razón social' },
    { key: 'sector',     titulo: 'Sector',
      render: e => <span style={{ fontSize: 11, padding: '3px 8px', background: '#e0e7ff', color: '#3730a3', borderRadius: 20, fontWeight: 500 }}>{e.sector}</span> },
    { key: 'municipio',  titulo: 'Municipio' },
    { key: 'activo',     titulo: 'Estado', render: e => <BadgeEstado activo={e.activo} /> },
    {
      key: 'acciones', titulo: 'Acciones',
      render: e => (
        <div style={{ display: 'flex', gap: 6 }}>
          <button
            onClick={() => e.activo ? inactivar.mutate(e.id) : activar.mutate(e.id)}
            style={{ padding: '5px 12px', background: e.activo ? '#fee2e2':'#d1fae5', color: e.activo ? '#991b1b':'#065f46', border: 'none', borderRadius: 6, cursor: 'pointer', fontSize: 12 }}>
            {e.activo ? 'Inactivar' : 'Activar'}
          </button>
        </div>
      )
    }
  ];

  return (
    <div style={{ fontFamily: 'Arial, sans-serif' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <h2 style={{ fontSize: 20, fontWeight: 700, color: '#1e3a5f', margin: 0 }}>
          Gestión de empresas
        </h2>
        <button onClick={() => setModal(true)}
          style={{ padding: '9px 18px', background: '#1e3a5f', color: '#fff', border: 'none', borderRadius: 8, cursor: 'pointer', fontSize: 13, fontWeight: 600 }}>
          + Registrar empresa
        </button>
      </div>

      <input
        placeholder="Buscar por nombre o NIT..."
        value={busqueda}
        onChange={e => setBusqueda(e.target.value)}
        style={{ padding: '8px 12px', border: '1px solid #d1d5db', borderRadius: 8, fontSize: 13, marginBottom: 14, width: 280 }}
      />

      <TablaBase columnas={columnas} datos={empresasFiltradas} cargando={isLoading} />

      {modal && (
        <ModalEmpresa
          onGuardar={(form) => { registrar.mutate(form); setModal(false); }}
          onCerrar={() => setModal(false)}
        />
      )}
    </div>
  );
}