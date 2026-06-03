import { useState } from 'react';
import useLogin from '../hooks/useLogin';
import { Link } from 'react-router-dom';

export default function LoginPage() {
  const [correo,   setCorreo]   = useState('');
  const [password, setPassword] = useState('');
  const [verPass,  setVerPass]  = useState(false);
  const { iniciarSesion, cargando, error } = useLogin();

  const handleSubmit = (e) => {
    e.preventDefault();
    iniciarSesion(correo, password);
  };

  return (
    <div style={estilos.contenedor}>
      <div style={estilos.tarjeta}>

        {/* Encabezado */}
        <div style={estilos.encabezado}>
          <h1 style={estilos.titulo}>Prácticas Empresariales</h1>
          <p style={estilos.subtitulo}>Universidad Alexander Von Humboldt</p>
        </div>

        {/* Formulario */}
        <form onSubmit={handleSubmit} style={estilos.form}>

          {error && (
            <div style={estilos.error}>
              ⚠ {error}
            </div>
          )}

          <div style={estilos.campo}>
            <label style={estilos.label}>Correo electrónico</label>
            <input
              type="email"
              value={correo}
              onChange={e => setCorreo(e.target.value)}
              placeholder="correo@avh.edu.co"
              required
              style={estilos.input}
            />
          </div>

          <div style={estilos.campo}>
            <label style={estilos.label}>Contraseña</label>
            <div style={estilos.inputConIcono}>
              <input
                type={verPass ? 'text' : 'password'}
                value={password}
                onChange={e => setPassword(e.target.value)}
                placeholder="••••••••"
                required
                style={{ ...estilos.input, flex: 1, marginBottom: 0 }}
              />
              <button
                type="button"
                onClick={() => setVerPass(!verPass)}
                style={estilos.btnVerPass}
              >
                {verPass ? '🙈' : '👁'}
              </button>
            </div>
          </div>

          <button
            type="submit"
            disabled={cargando}
            style={cargando ? estilos.btnDeshabilitado : estilos.btn}
          >
            {cargando ? 'Iniciando sesión...' : 'Iniciar sesión'}
          </button>

          <div style={{ textAlign: 'center', marginTop: 12 }}>
            <Link to="/recuperar-password" style={estilos.link}>
              ¿Olvidaste tu contraseña?
            </Link>
          </div>

        </form>
      </div>
    </div>
  );
}

const estilos = {
  contenedor: {
    minHeight: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    background: '#f0f4f8',
    fontFamily: 'Arial, sans-serif'
  },
  tarjeta: {
    background: '#ffffff',
    borderRadius: 12,
    padding: '40px 36px',
    width: '100%',
    maxWidth: 400,
    boxShadow: '0 4px 20px rgba(0,0,0,0.1)'
  },
  encabezado: {
    textAlign: 'center',
    marginBottom: 28
  },
  titulo: {
    fontSize: 22,
    fontWeight: 700,
    color: '#1e3a5f',
    margin: 0
  },
  subtitulo: {
    fontSize: 13,
    color: '#6b7280',
    margin: '4px 0 0'
  },
  form: {
    display: 'flex',
    flexDirection: 'column',
    gap: 16
  },
  campo: {
    display: 'flex',
    flexDirection: 'column',
    gap: 6
  },
  label: {
    fontSize: 13,
    fontWeight: 600,
    color: '#374151'
  },
  input: {
    padding: '10px 12px',
    border: '1px solid #d1d5db',
    borderRadius: 8,
    fontSize: 14,
    outline: 'none',
    width: '100%',
    boxSizing: 'border-box'
  },
  inputConIcono: {
    display: 'flex',
    alignItems: 'center',
    gap: 8
  },
  btnVerPass: {
    background: 'none',
    border: 'none',
    cursor: 'pointer',
    fontSize: 18,
    padding: '0 4px'
  },
  btn: {
    padding: '11px',
    background: '#1e3a5f',
    color: '#fff',
    border: 'none',
    borderRadius: 8,
    fontSize: 15,
    fontWeight: 600,
    cursor: 'pointer',
    marginTop: 4
  },
  btnDeshabilitado: {
    padding: '11px',
    background: '#9ca3af',
    color: '#fff',
    border: 'none',
    borderRadius: 8,
    fontSize: 15,
    cursor: 'not-allowed',
    marginTop: 4
  },
  error: {
    background: '#fef2f2',
    border: '1px solid #fca5a5',
    borderRadius: 8,
    padding: '10px 12px',
    fontSize: 13,
    color: '#dc2626'
  },
  link: {
    color: '#1e3a5f',
    fontSize: 13,
    textDecoration: 'none'
  }
};