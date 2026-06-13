import { useState } from 'react';
import useUsuarios    from '../hooks/useUsuarios';
import FiltrosUsuario from './FiltrosUsuario';
import ModalUsuario   from './ModalUsuario';
import TablaBase      from '../../../shared/components/TablaBase';
import BadgeEstado    from '../../../shared/components/BadgeEstado';
import Paginacion     from '../../../shared/components/Paginacion';

export default function UsuariosPage() {
  const { usuarios, isLoading, filtros, setFiltros, totalPaginas, irAPagina,
          crear, editar, activar, inactivar } = useUsuarios();
  const [modal,    setModal]    = useState(false);
  const [editando, setEditando] = useState(null);

  const abrirCrear  = ()  => { setEditando(null); setModal(true); };
  const abrirEditar = (u) => { setEditando(u);    setModal(true); };
  const cerrar      = ()  => setModal(false);

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
    { key: 'rol',    titulo: 'Rol',
      render: u => <span style={estilos.badgeRol}>{u.rol}</span> },
    { key: 'activo', titulo: 'Estado',
      render: u => <BadgeEstado activo={u.activo} /> },
    {
      key: 'acciones', titulo: 'Acciones',
      render: u => (
        <div style={{ display: 'flex', gap: 6 }}>
          <button onClick={() => abrirEditar(u)} style={btn('#dbeafe','#1e40af')}>
            Editar
          </button>
          <button
            onClick={() => u.activo ? inactivar.mutate(u.id) : activar.mutate(u.id)}
            style={btn(u.activo ? '#fee2e2':'#d1fae5', u.activo ? '#991b1b':'#065f46')}
          >
            {u.activo ? 'Inactivar' : 'Activar'}
          </button>
        </div>
      )
    }
  ];

  return (
    <div style={{ fontFamily: 'Arial, sans-serif' }}>
      <div style={estilos.encabezado}>
        <h2 style={estilos.titulo}>Gestión de usuarios</h2>
        <button onClick={abrirCrear} style={estilos.btnPrimario}>+ Nuevo usuario</button>
      </div>

      <FiltrosUsuario filtros={filtros} onChange={setFiltros} />
      <TablaBase columnas={columnas} datos={usuarios} cargando={isLoading} />
      <Paginacion
        pagina={filtros.page}
        totalPaginas={totalPaginas}
        onCambiarPagina={irAPagina}
      />

      {modal && (
        <ModalUsuario
          usuario={editando}
          onGuardar={guardar}
          onCerrar={cerrar}
        />
      )}
    </div>
  );
}

const btn = (bg, color) => ({
  padding: '5px 12px', background: bg, color,
  border: 'none', borderRadius: 6, cursor: 'pointer', fontSize: 12, fontWeight: 500
});

const estilos = {
  encabezado:  { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 },
  titulo:      { fontSize: 20, fontWeight: 700, color: '#1e3a5f', margin: 0 },
  btnPrimario: { padding: '9px 18px', background: '#1e3a5f', color: '#fff', border: 'none', borderRadius: 8, cursor: 'pointer', fontSize: 13, fontWeight: 600 },
  badgeRol:    { fontSize: 11, padding: '3px 8px', background: '#e0e7ff', color: '#3730a3', borderRadius: 20, fontWeight: 500 }
};