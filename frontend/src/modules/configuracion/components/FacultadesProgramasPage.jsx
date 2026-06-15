import { useState } from 'react';
import FacultadesPage from './FacultadesPage';
import ProgramasPage from './ProgramasPage';
import { PageHeader } from '@/shared/components/ui';

export default function FacultadesProgramasPage() {
  const [tab, setTab] = useState('facultades');

  return (
    <div>
      <PageHeader
        titulo="Facultades y programas"
        descripcion="Administra el catálogo académico del sistema."
      />

      <div className="mb-6 flex gap-6 border-b border-gray-200">
        {[
          { id: 'facultades', label: 'Facultades' },
          { id: 'programas', label: 'Programas' },
        ].map(({ id, label }) => (
          <button
            key={id}
            type="button"
            onClick={() => setTab(id)}
            className={[
              '-mb-px border-b-2 px-1 pb-3 text-sm font-semibold transition-colors',
              tab === id
                ? 'border-primary text-primary'
                : 'border-transparent text-gray-500 hover:text-gray-700',
            ].join(' ')}
          >
            {label}
          </button>
        ))}
      </div>

      <div className="rounded-xl border border-gray-200 bg-white p-4 sm:p-6">
        {tab === 'facultades' ? (
          <FacultadesPage esSubComponente />
        ) : (
          <ProgramasPage esSubComponente />
        )}
      </div>
    </div>
  );
}
