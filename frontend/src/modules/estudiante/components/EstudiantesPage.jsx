import { useState } from 'react';
import useEstudiantes from '../hooks/useEstudiantes';
import FiltrosEstudiante from './FiltrosEstudiante';
import ModalRegistroEstudiante from './ModalRegistroEstudiante';
import ImportarExcel from './ImportarExcel';
import TablaBase from '../../../shared/components/TablaBase';
import BadgeEstado from '../../../shared/components/BadgeEstado';
import Paginacion from '../../../shared/components/Paginacion';
import { Badge, Button, PageHeader } from '@/shared/components/ui';

const variantAptitud = {
  APTO: 'success',
  NO_APTO: 'danger',
  SIN_EVALUAR: 'warning',
};

export default function EstudiantesPage() {
  const {
    estudiantes,
    isLoading,
    filtros,
    setFiltros,
    totalPaginas,
    irAPagina,
    registrar,
    marcarApto,
    marcarNoApto,
  } = useEstudiantes();
  const [modalRegistro, setModalRegistro] = useState(false);
  const [modalImportar, setModalImportar] = useState(false);

  const columnas = [
    { key: 'nombre', titulo: 'Nombre' },
    { key: 'identificacion', titulo: 'Identificación' },
    { key: 'programa', titulo: 'Programa', render: (e) => e.programa?.nombre ?? '—' },
    { key: 'semestre', titulo: 'Semestre' },
    {
      key: 'estadoAptitud',
      titulo: 'Aptitud',
      render: (e) => (
        <Badge variant={variantAptitud[e.estadoAptitud] ?? 'warning'}>
          {e.estadoAptitud}
        </Badge>
      ),
    },
    { key: 'activo', titulo: 'Estado', render: (e) => <BadgeEstado activo={e.activo} /> },
    {
      key: 'acciones',
      titulo: 'Acciones',
      render: (e) => (
        <div className="flex flex-wrap gap-1.5">
          {e.estadoAptitud === 'SIN_EVALUAR' && (
            <>
              <Button variant="success" size="sm" onClick={() => marcarApto.mutate(e.id)}>
                Apto
              </Button>
              <Button
                variant="danger"
                size="sm"
                onClick={() => marcarNoApto.mutate({ id: e.id, motivo: 'Sin requisitos' })}
              >
                No apto
              </Button>
            </>
          )}
        </div>
      ),
    },
  ];

  return (
    <div>
      <PageHeader
        titulo="Gestión de estudiantes"
        acciones={
          <>
            <Button variant="success" onClick={() => setModalImportar(true)}>
              Importar Excel
            </Button>
            <Button onClick={() => setModalRegistro(true)}>+ Registrar</Button>
          </>
        }
      />

      <FiltrosEstudiante filtros={filtros} onChange={setFiltros} />
      <TablaBase columnas={columnas} datos={estudiantes} cargando={isLoading} />
      <Paginacion pagina={filtros.page} totalPaginas={totalPaginas} onCambiarPagina={irAPagina} />

      {modalRegistro && (
        <ModalRegistroEstudiante
          guardando={registrar.isPending}
          onGuardar={(dto) => registrar.mutate(dto, { onSuccess: () => setModalRegistro(false) })}
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
