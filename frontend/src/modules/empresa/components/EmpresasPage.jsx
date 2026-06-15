import { useState } from 'react';
import { useEmpresas } from '../hooks/useEmpresas';
import ModalEmpresa from './ModalEmpresa';
import TablaBase from '../../../shared/components/TablaBase';
import BadgeEstado from '../../../shared/components/BadgeEstado';
import Paginacion from '../../../shared/components/Paginacion';
import { Badge, Button, Input, PageHeader } from '@/shared/components/ui';

export default function EmpresasPage() {
  const {
    empresas,
    isLoading,
    filtros,
    setFiltros,
    totalPaginas,
    irAPagina,
    registrar,
    activar,
    inactivar,
  } = useEmpresas();
  const [modal, setModal] = useState(false);

  const actualizarBusqueda = (busqueda) => {
    setFiltros((prev) => ({
      ...prev,
      busqueda: busqueda || undefined,
      page: 0,
    }));
  };

  const columnas = [
    { key: 'nit', titulo: 'NIT' },
    { key: 'razonSocial', titulo: 'Razón social' },
    {
      key: 'sector',
      titulo: 'Sector',
      render: (e) => <Badge variant="purple">{e.sector}</Badge>,
    },
    { key: 'municipio', titulo: 'Municipio' },
    { key: 'activo', titulo: 'Estado', render: (e) => <BadgeEstado activo={e.activo} /> },
    {
      key: 'acciones',
      titulo: 'Acciones',
      render: (e) => (
        <Button
          variant={e.activo ? 'danger' : 'success'}
          size="sm"
          onClick={() => (e.activo ? inactivar.mutate(e.id) : activar.mutate(e.id))}
        >
          {e.activo ? 'Inactivar' : 'Activar'}
        </Button>
      ),
    },
  ];

  return (
    <div>
      <PageHeader
        titulo="Gestión de empresas"
        acciones={<Button onClick={() => setModal(true)}>+ Registrar empresa</Button>}
      />

      <Input
        className="mb-4 max-w-xs"
        placeholder="Buscar por nombre o NIT..."
        value={filtros.busqueda || ''}
        onChange={(e) => actualizarBusqueda(e.target.value)}
      />

      <TablaBase columnas={columnas} datos={empresas} cargando={isLoading} />
      <Paginacion pagina={filtros.page} totalPaginas={totalPaginas} onCambiarPagina={irAPagina} />

      {modal && (
        <ModalEmpresa
          onGuardar={(form) => {
            registrar.mutate(form);
            setModal(false);
          }}
          onCerrar={() => setModal(false)}
        />
      )}
    </div>
  );
}
