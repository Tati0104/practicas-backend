import { useState } from 'react';
import useUsuarios from '../hooks/useUsuarios';
import FiltrosUsuario from './FiltrosUsuario';
import ModalUsuario from './ModalUsuario';
import TablaBase from '../../../shared/components/TablaBase';
import BadgeEstado from '../../../shared/components/BadgeEstado';
import Paginacion from '../../../shared/components/Paginacion';
import { Badge, Button, PageHeader } from '@/shared/components/ui';
import { etiquetaRol } from '../constants/catalogoUsuario';

export default function UsuariosPage() {
  const {
    usuarios,
    isLoading,
    filtros,
    setFiltros,
    totalPaginas,
    irAPagina,
    crear,
    editar,
    activar,
    inactivar,
  } = useUsuarios();
  const [modal, setModal] = useState(false);
  const [editando, setEditando] = useState(null);

  const abrirCrear = () => {
    setEditando(null);
    setModal(true);
  };
  const abrirEditar = (u) => {
    setEditando(u);
    setModal(true);
  };
  const cerrar = () => setModal(false);

  const guardar = (form) => {
    if (editando) {
      editar.mutate({ id: editando.id, dto: form });
    } else {
      crear.mutate(form);
    }
    cerrar();
  };

  const columnas = [
    { key: 'nombre', titulo: 'Nombre' },
    { key: 'correo', titulo: 'Correo' },
    {
      key: 'rol',
      titulo: 'Rol',
      render: (u) => <Badge variant="purple">{etiquetaRol(u.rol)}</Badge>,
    },
    { key: 'activo', titulo: 'Estado', render: (u) => <BadgeEstado activo={u.activo} /> },
    {
      key: 'acciones',
      titulo: 'Acciones',
      render: (u) => (
        <div className="flex flex-wrap gap-1.5">
          <Button variant="info" size="sm" onClick={() => abrirEditar(u)}>
            Editar
          </Button>
          <Button
            variant={u.activo ? 'danger' : 'success'}
            size="sm"
            onClick={() => (u.activo ? inactivar.mutate(u.id) : activar.mutate(u.id))}
          >
            {u.activo ? 'Inactivar' : 'Activar'}
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div>
      <PageHeader
        titulo="Gestión de usuarios"
        acciones={<Button onClick={abrirCrear}>+ Nuevo usuario</Button>}
      />

      <FiltrosUsuario filtros={filtros} onChange={setFiltros} />
      <TablaBase columnas={columnas} datos={usuarios} cargando={isLoading} />
      <Paginacion pagina={filtros.page} totalPaginas={totalPaginas} onCambiarPagina={irAPagina} />

      {modal && <ModalUsuario usuario={editando} onGuardar={guardar} onCerrar={cerrar} />}
    </div>
  );
}
