import { useState } from 'react';
import { useEmpresas } from '../hooks/useEmpresas';
import ModalEmpresa from './ModalEmpresa';
import ModalDetalleEmpresa from './ModalDetalleEmpresa';
import ModalInactivarEmpresa from './ModalInactivarEmpresa';
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
    editar,
    activar,
    inactivar,
  } = useEmpresas();

  const [modalForm, setModalForm] = useState(false);
  const [editando, setEditando] = useState(null);
  const [detalle, setDetalle] = useState(null);
  const [inactivando, setInactivando] = useState(null);

  const abrirRegistrar = () => {
    setEditando(null);
    setModalForm(true);
  };

  const abrirEditar = (empresa) => {
    setEditando(empresa);
    setModalForm(true);
  };

  const abrirDetalle = (empresa) => {
    setDetalle(empresa);
  };

  const abrirInactivar = (empresa) => {
    setInactivando(empresa);
  };

  const cerrarForm = () => {
    setModalForm(false);
    setEditando(null);
  };

  const cerrarDetalle = () => setDetalle(null);

  const cerrarInactivar = () => setInactivando(null);

  const confirmarInactivacion = (motivo) => {
    inactivar.mutate(
      { id: inactivando.id, motivo },
      {
        onSuccess: () => {
          cerrarInactivar();
          if (detalle?.id === inactivando.id) cerrarDetalle();
        },
      }
    );
  };

  const reactivarEmpresa = (empresa) => {
    activar.mutate(empresa.id, {
      onSuccess: () => {
        setDetalle((prev) =>
          prev?.id === empresa.id
            ? { ...prev, activo: true, motivoInactivacion: null, fechaInactivacion: null }
            : prev
        );
      },
    });
  };

  const guardando = registrar.isPending || editar.isPending;

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
      render: (e) => <Badge variant="purple">{e.sector?.nombre ?? '—'}</Badge>,
    },
    { key: 'municipio', titulo: 'Municipio' },
    { key: 'activo', titulo: 'Estado', render: (e) => <BadgeEstado activo={e.activo} /> },
    {
      key: 'acciones',
      titulo: 'Acciones',
      render: (e) => (
        <div className="flex flex-wrap gap-1.5">
          <Button variant="ghost" size="sm" onClick={() => abrirDetalle(e)}>
            Ver
          </Button>
          <Button variant="info" size="sm" onClick={() => abrirEditar(e)}>
            Editar
          </Button>
          {e.activo ? (
            <Button variant="danger" size="sm" onClick={() => abrirInactivar(e)}>
              Inactivar
            </Button>
          ) : (
            <Button variant="success" size="sm" onClick={() => reactivarEmpresa(e)}>
              Activar
            </Button>
          )}
        </div>
      ),
    },
  ];

  return (
    <div>
      <PageHeader
        titulo="Gestión de empresas"
        acciones={<Button onClick={abrirRegistrar}>+ Registrar empresa</Button>}
      />

      <Input
        className="mb-4 max-w-xs"
        placeholder="Buscar por nombre o NIT..."
        value={filtros.busqueda || ''}
        onChange={(e) => actualizarBusqueda(e.target.value)}
      />

      <TablaBase columnas={columnas} datos={empresas} cargando={isLoading} />
      <Paginacion pagina={filtros.page} totalPaginas={totalPaginas} onCambiarPagina={irAPagina} />

      {modalForm && (
        <ModalEmpresa
          empresa={editando}
          guardando={guardando}
          onGuardar={(form) => {
            if (editando) {
              editar.mutate({ id: editando.id, dto: form }, { onSuccess: cerrarForm });
            } else {
              registrar.mutate(form, { onSuccess: cerrarForm });
            }
          }}
          onCerrar={cerrarForm}
        />
      )}

      {detalle && (
        <ModalDetalleEmpresa
          empresa={detalle}
          onCerrar={cerrarDetalle}
          onInactivar={(empresa) => {
            cerrarDetalle();
            abrirInactivar(empresa);
          }}
          onActivar={reactivarEmpresa}
          activando={activar.isPending}
        />
      )}

      {inactivando && (
        <ModalInactivarEmpresa
          empresa={inactivando}
          guardando={inactivar.isPending}
          onConfirmar={confirmarInactivacion}
          onCerrar={cerrarInactivar}
        />
      )}
    </div>
  );
}
