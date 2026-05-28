import { useState, useEffect } from 'react';

const ROLES  = ['ADMIN','DIRECCION','COORD_ACADEMICA','COORD_PRACTICA',
                'SECRETARIA','DOCENTE_ASESOR','EMPRESA','TUTOR_EMPRESARIAL','ESTUDIANTE'];
const SCOPES = ['GLOBAL','FACULTAD','PROGRAMA','ASIGNADO'];

export default function ModalUsuario({ usuario, onGuardar, onCerrar }) {
  const [form,  setForm]  = useState({ nombre: '', correo: '', rol: '', scope: '' });
  const [error, setError] = useState('');

  useEffect(() => {
    if (usuario) {
      setForm({ nombre: usuario.nombre, correo: usuario.correo,
                rol: usuario.rol, scope: usuario.scope });
    } else {
      setForm({ nombre: '', correo: '', rol: '', scope: '' });
    }
  }, [usuario]);

  const campo = (key, value) => setForm(f => ({ ...f, [key]: value }));

  const guardar = () => {
    if (!form.nombre.trim()) { setError('El nombre es obligatorio');  return; }
    if (!form.correo.trim()) { setError('El correo es obligatorio');  return; }
    if (!form.rol)           { setError('El rol es obligatorio');     return; }
    if (!form.scope)         { setError('El scope es obligatorio');   return; }
    setError('');
    onGuardar(form);
  };

  return (
    <div style={estilos.overlay}>
      <div style={estilos.modal}>
        <h3 style={estilos.titulo}>
          {usuario ? 'Editar usuario' : 'Nuevo usuario'}
        </h3>

        {error && <p style={estilos.error}>⚠ {error}</p>}

        <div style={estilos.grid}>
          <div style={estilos.campo}>
            <label style={estilos.label}>Nombre completo</label>
            <input value={form.nombre} onChange={e => campo('nombre', e.target.value)}
              style={estilos.input} placeholder="Ej: Juan García" />
          </div>

          <div style={estilos.campo}>
            <label style={estilos.label}>Correo electrónico</label>
            <input value={form.correo} onChange={e => campo('correo', e.target.value)}
              style={estilos.input} placeholder="correo@avh.edu.co"
              disabled={!!usuario} />
          </div>

          <div style={estilos.campo}>
            <label style={estilos.label}>Rol</label>
            <select value={form.rol} onChange={e => campo('rol', e.target.value)} style={estilos.input}>
              <option value="">Seleccionar rol</option>
              {ROLES.map(r => <option key={r} value={r}>{r}</option>)}
            </select>
          </div>

          <div style={estilos.campo}>
            <label style={estilos.label}>Scope</label>
            <select value={form.scope} onChange={e => campo('scope', e.target.value)} style={estilos.input}>
              <option value="">Seleccionar scope</option>
              {SCOPES.map(s => <option key={s} value={s}>{s}</option>)}
            </select>
          </div>
        </div>

        {!usuario && (
          <p style={estilos.nota}>
            📧 Se enviará una contraseña temporal al correo del usuario.
          </p>
        )}

        <div style={estilos.btns}>
          <button onClick={onCerrar} style={estilos.btnCancelar}>Cancelar</button>
          <button onClick={guardar}  style={estilos.btnGuardar}>
            {usuario ? 'Guardar cambios' : 'Crear usuario'}
          </button>
        </div>
      </div>
    </div>
  );
}

const estilos = {
  overlay:     { position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.45)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 200 },
  modal:       { background: '#fff', borderRadius: 12, padding: 28, width: 480, display: 'flex', flexDirection: 'column', gap: 14, maxHeight: '90vh', overflowY: 'auto' },
  titulo:      { fontSize: 17, fontWeight: 700, color: '#1e3a5f', margin: 0 },
  error:       { background: '#fef2f2', border: '1px solid #fca5a5', borderRadius: 8, padding: '8px 12px', fontSize: 13, color: '#dc2626', margin: 0 },
  grid:        { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 },
  campo:       { display: 'flex', flexDirection: 'column', gap: 5 },
  label:       { fontSize: 12, fontWeight: 600, color: '#374151' },
  input:       { padding: '9px 11px', border: '1px solid #d1d5db', borderRadius: 8, fontSize: 13, fontFamily: 'Arial' },
  nota:        { fontSize: 12, color: '#6b7280', background: '#f0f9ff', padding: '8px 12px', borderRadius: 8, margin: 0 },
  btns:        { display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 4 },
  btnCancelar: { padding: '9px 18px', background: '#f3f4f6', color: '#374151', border: 'none', borderRadius: 8, cursor: 'pointer', fontSize: 13 },
  btnGuardar:  { padding: '9px 18px', background: '#1e3a5f', color: '#fff',    border: 'none', borderRadius: 8, cursor: 'pointer', fontSize: 13, fontWeight: 600 }
};