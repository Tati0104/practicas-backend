import useAuth from '../hooks/useAuth';

const nombreRol = {
  ADMIN:            'Administrador',
  DIRECCION:        'Dirección',
  COORD_ACADEMICA:  'Coordinación Académica',
  COORD_PRACTICA:   'Coordinador de Práctica',
  SECRETARIA:       'Secretaría',
  DOCENTE_ASESOR:   'Docente Asesor',
  EMPRESA:          'Empresa',
  TUTOR_EMPRESARIAL:'Tutor Empresarial',
  ESTUDIANTE:       'Estudiante'
};

export default function Navbar() {
  const { usuario } = useAuth();

  return (
    <header style={estilos.navbar}>
      <span style={estilos.bienvenida}>
        Bienvenido/a, <strong>{usuario?.nombre}</strong>
      </span>
      <span style={estilos.rol}>
        {nombreRol[usuario?.rol] || usuario?.rol}
      </span>
    </header>
  );
}

const estilos = {
  navbar: {
    height: 56,
    background: '#ffffff',
    borderBottom: '1px solid #e5e7eb',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: '0 28px',
    fontFamily: 'Arial, sans-serif'
  },
  bienvenida: {
    fontSize: 14,
    color: '#374151'
  },
  rol: {
    fontSize: 12,
    background: '#dbeafe',
    color: '#1e40af',
    padding: '4px 10px',
    borderRadius: 20,
    fontWeight: 500
  }
};