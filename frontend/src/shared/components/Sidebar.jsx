import { NavLink, useNavigate } from 'react-router-dom';
import useAuth from '../hooks/useAuth';

const menuPorRol = {
  ADMIN: [
    { ruta: '/dashboard',          icono: '🏠', nombre: 'Inicio' },
    { ruta: '/admin/usuarios',     icono: '👥', nombre: 'Usuarios' },
    { ruta: '/configuracion/facultades', icono: '🏫', nombre: 'Facultades' },
    { ruta: '/configuracion/programas',  icono: '📚', nombre: 'Programas' },
    { ruta: '/estudiantes',        icono: '🎓', nombre: 'Estudiantes' },
    { ruta: '/empresas',           icono: '🏢', nombre: 'Empresas' },
    { ruta: '/vacantes',           icono: '📋', nombre: 'Vacantes' },
    { ruta: '/asignaciones',       icono: '🔗', nombre: 'Asignaciones' },
    { ruta: '/vinculacion',        icono: '📄', nombre: 'Vinculación' },
    { ruta: '/seguimiento',        icono: '📊', nombre: 'Seguimiento' },
  ],
  COORD_ACADEMICA: [
    { ruta: '/dashboard',          icono: '🏠', nombre: 'Inicio' },
    { ruta: '/configuracion/programas',  icono: '📚', nombre: 'Programas' },
    { ruta: '/estudiantes',        icono: '🎓', nombre: 'Estudiantes' },
  ],
  COORD_PRACTICA: [
    { ruta: '/dashboard',          icono: '🏠', nombre: 'Inicio' },
    { ruta: '/estudiantes',        icono: '🎓', nombre: 'Estudiantes' },
    { ruta: '/empresas',           icono: '🏢', nombre: 'Empresas' },
    { ruta: '/vacantes',           icono: '📋', nombre: 'Vacantes' },
    { ruta: '/asignaciones',       icono: '🔗', nombre: 'Asignaciones' },
    { ruta: '/vinculacion',        icono: '📄', nombre: 'Vinculación' },
    { ruta: '/seguimiento',        icono: '📊', nombre: 'Seguimiento' },
  ],
  SECRETARIA: [
    { ruta: '/dashboard',          icono: '🏠', nombre: 'Inicio' },
    { ruta: '/estudiantes',        icono: '🎓', nombre: 'Estudiantes' },
    { ruta: '/empresas',           icono: '🏢', nombre: 'Empresas' },
    { ruta: '/vacantes',           icono: '📋', nombre: 'Vacantes' },
  ],
  DOCENTE_ASESOR: [
    { ruta: '/dashboard',          icono: '🏠', nombre: 'Inicio' },
    { ruta: '/seguimiento',        icono: '📊', nombre: 'Seguimiento' },
  ],
  EMPRESA: [
    { ruta: '/dashboard',          icono: '🏠', nombre: 'Inicio' },
    { ruta: '/vacantes',           icono: '📋', nombre: 'Vacantes' },
  ],
  TUTOR_EMPRESARIAL: [
    { ruta: '/dashboard',          icono: '🏠', nombre: 'Inicio' },
    { ruta: '/seguimiento',        icono: '📊', nombre: 'Seguimiento' },
    { ruta: '/vinculacion',        icono: '📄', nombre: 'Vinculación' },
  ],
  ESTUDIANTE: [
    { ruta: '/dashboard',          icono: '🏠', nombre: 'Inicio' },
    { ruta: '/seguimiento',        icono: '📊', nombre: 'Seguimiento' },
    { ruta: '/vinculacion',        icono: '📄', nombre: 'Vinculación' },
  ],
  DIRECCION: [
    { ruta: '/dashboard',          icono: '🏠', nombre: 'Inicio' },
  ],
};

export default function Sidebar() {
  const { usuario, rol, nombre, logout } = useAuth();
  const navigate = useNavigate();
  const rolActivo = rol ?? usuario?.rol;
  const menu = menuPorRol[rolActivo] || [];

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <aside style={estilos.sidebar}>
      <div style={estilos.logo}>
        <span style={estilos.logoTexto}>AVH</span>
        <span style={estilos.logoSub}>Prácticas</span>
      </div>

      <nav style={estilos.nav}>
        {menu.map(item => (
          <NavLink
            key={item.ruta}
            to={item.ruta}
            style={({ isActive }) =>
              isActive ? { ...estilos.link, ...estilos.linkActivo } : estilos.link
            }
          >
            <span style={estilos.icono}>{item.icono}</span>
            {item.nombre}
          </NavLink>
        ))}
      </nav>

      <button onClick={handleLogout} style={estilos.btnLogout}>
        🚪 Cerrar sesión
      </button>
    </aside>
  );
}

const estilos = {
  sidebar: {
    width: 220,
    minHeight: '100vh',
    background: '#1e3a5f',
    display: 'flex',
    flexDirection: 'column',
    padding: '0 0 16px',
    position: 'fixed',
    top: 0, left: 0, bottom: 0,
    zIndex: 100
  },
  logo: {
    padding: '24px 20px 20px',
    borderBottom: '1px solid rgba(255,255,255,0.1)',
    marginBottom: 12
  },
  logoTexto: {
    display: 'block',
    fontSize: 22,
    fontWeight: 700,
    color: '#ffffff'
  },
  logoSub: {
    fontSize: 12,
    color: '#93c5fd'
  },
  nav: {
    flex: 1,
    display: 'flex',
    flexDirection: 'column',
    gap: 2,
    padding: '0 10px'
  },
  link: {
    display: 'flex',
    alignItems: 'center',
    gap: 10,
    padding: '10px 12px',
    borderRadius: 8,
    color: '#cbd5e1',
    textDecoration: 'none',
    fontSize: 14,
    fontFamily: 'Arial, sans-serif'
  },
  linkActivo: {
    background: 'rgba(255,255,255,0.15)',
    color: '#ffffff',
    fontWeight: 600
  },
  icono: { fontSize: 16 },
  btnLogout: {
    margin: '0 10px',
    padding: '10px 12px',
    background: 'rgba(255,255,255,0.08)',
    border: '1px solid rgba(255,255,255,0.15)',
    borderRadius: 8,
    color: '#cbd5e1',
    cursor: 'pointer',
    fontSize: 13,
    textAlign: 'left',
    fontFamily: 'Arial, sans-serif'
  }
};