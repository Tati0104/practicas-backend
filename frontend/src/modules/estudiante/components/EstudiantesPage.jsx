import { useState } from 'react';
import useEstudiantes from '../hooks/useEstudiantes';
import FiltrosEstudiante from './FiltrosEstudiante';
import ModalRegistroEstudiante from './ModalRegistroEstudiante';
import ImportarExcel from './ImportarExcel';
import TablaBase from '../../../shared/components/TablaBase';
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
    editar,
    marcarApto,
    marcarNoApto,
  } = useEstudiantes();
  const [modalRegistro, setModalRegistro] = useState(false);
  const [editando, setEditando] = useState(null);
  const [modalImportar, setModalImportar] = useState(false);

  const abrirRegistrar = () => {
    setEditando(null);
    setModalRegistro(true);
  };

  const abrirEditar = (estudiante) => {
    setEditando(estudiante);
    setModalRegistro(true);
  };

  const cerrarModal = () => {
    setModalRegistro(false);
    setEditando(null);
  };

  const guardando = registrar.isPending || editar.isPending;

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
    {
      key: 'acciones',
      titulo: 'Acciones',
      render: (e) => (
        <div className="flex flex-wrap gap-1.5">
          <Button variant="info" size="sm" onClick={() => abrirEditar(e)}>
            Editar
          </Button>
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
            <Button onClick={abrirRegistrar}>+ Registrar</Button>
          </>
        }
      />

      <FiltrosEstudiante filtros={filtros} onChange={setFiltros} />
      <TablaBase columnas={columnas} datos={estudiantes} cargando={isLoading} />
      <Paginacion pagina={filtros.page} totalPaginas={totalPaginas} onCambiarPagina={irAPagina} />

      {modalRegistro && (
        <ModalRegistroEstudiante
          estudiante={editando}
          guardando={guardando}
          onGuardar={(dto) => {
            if (editando) {
              editar.mutate({ id: editando.id, dto }, { onSuccess: cerrarModal });
            } else {
              registrar.mutate(dto, { onSuccess: cerrarModal });
            }
          }}
          onCerrar={cerrarModal}
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
