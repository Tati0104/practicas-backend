import { useState } from 'react';
import useEstudiantes          from '../hooks/useEstudiantes';
import FiltrosEstudiante       from './FiltrosEstudiante';
import ModalRegistroEstudiante from './ModalRegistroEstudiante';
import ImportarExcel           from './ImportarExcel';
import TablaBase               from '../../../shared/components/TablaBase';
import BadgeEstado             from '../../../shared/components/BadgeEstado';

const colorAptitud = {
  APTO:         { bg: '#d1fae5', color: '#065f46' },
  NO_APTO:      { bg: '#fee2e2', color: '#991b1b' },
  SIN_EVALUAR:  { bg: '#fef3c7', color: '#92400e' }
};

export default function EstudiantesPage() {
  const { estudiantes, isLoading, filtros, setFiltros,
          registrar, marcarApto, marcarNoApto } = useEstudiantes();
  const [modalRegistro, setModalRegistro] = useState(false);
  const [modalImportar, setModalImportar] = useState(false);

  const columnas = [
    { key: 'nombre',         titulo: 'Nombre' },
    { key: 'identificacion', titulo: 'Identificación' },
    { key: 'programa',       titulo: 'Programa' },
    { key: 'semestre',       titulo: 'Semestre' },
    {
      key: 'estadoAptitud', titulo: 'Aptitud',
      render: e => {
        const c = colorAptitud[e.estadoAptitud] || colorAptitud.SIN_EVALUAR;
        return (
          <span style={{ fontSize: 11, fontWeight: 600, padding: '3px 10px',
            borderRadius: 20, background: c.bg, color: c.color }}>
            {e.estadoAptitud}
          </span>
        );
      }
    },
    { key: 'activo', titulo: 'Estado', render: e => <BadgeEstado activo={e.activo} /> },
    {
      key: 'acciones', titulo: 'Acciones',
      render: e => (
        <div style={{ display: 'flex', gap: 6 }}>
          {e.estadoAptitud === 'SIN_EVALUAR' && (
            <>
              <button
                onClick={() => marcarApto.mutate(e.id)}
                style={{ padding: '4px 10px', background: '#d1fae5', color: '#065f46', border: 'none', borderRadius: 6, cursor: 'pointer', fontSize: 11, fontWeight: 600 }}>
                ✓ Apto
              </button>
              <button
                onClick={() => marcarNoApto.mutate({ id: e.id, motivo: 'Sin requisitos' })}
                style={{ padding: '4px 10px', background: '#fee2e2', color: '#991b1b', border: 'none', borderRadius: 6, cursor: 'pointer', fontSize: 11, fontWeight: 600 }}>
                ✗ No apto
              </button>
            </>
          )}
        </div>
      )
    }
  ];

  return (
    <div style={{ fontFamily: 'Arial, sans-serif' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <h2 style={{ fontSize: 20, fontWeight: 700, color: '#1e3a5f', margin: 0 }}>
          Gestión de estudiantes
        </h2>
        <div style={{ display: 'flex', gap: 8 }}>
          <button onClick={() => setModalImportar(true)}
            style={{ padding: '9px 16px', background: '#059669', color: '#fff', border: 'none', borderRadius: 8, cursor: 'pointer', fontSize: 13, fontWeight: 600 }}>
            📂 Importar Excel
          </button>
          <button onClick={() => setModalRegistro(true)}
            style={{ padding: '9px 16px', background: '#1e3a5f', color: '#fff', border: 'none', borderRadius: 8, cursor: 'pointer', fontSize: 13, fontWeight: 600 }}>
            + Registrar
          </button>
        </div>
      </div>

      <FiltrosEstudiante filtros={filtros} onChange={setFiltros} />
      <TablaBase columnas={columnas} datos={estudiantes} cargando={isLoading} />

      {modalRegistro && (
        <ModalRegistroEstudiante
          onGuardar={(form) => { registrar.mutate(form); setModalRegistro(false); }}
          onCerrar={() => setModalRegistro(false)}
        />
      )}
      {modalImportar && (
        <ImportarExcel
          onImportar={() => setModalImportar(false)}
          onCerrar={() => setModalImportar(false)}
        />
      )}
    </div>
  );
}