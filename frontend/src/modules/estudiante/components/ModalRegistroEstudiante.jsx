import { useState } from 'react';

export default function ModalRegistroEstudiante({ onGuardar, onCerrar }) {
  const [form,  setForm]  = useState({
    nombre: '', identificacion: '', correo: '', telefono: '',
    programa: '', semestre: '', creditosAprobados: '', promedioAcumulado: ''
  });
  const [error, setError] = useState('');
  const campo = (k, v) => setForm(f => ({ ...f, [k]: v }));

  const guardar = () => {
    if (!form.nombre.trim())         { setError('El nombre es obligatorio');          return; }
    if (!form.identificacion.trim()) { setError('La identificación es obligatoria');  return; }
    if (!form.correo.trim())         { setError('El correo es obligatorio');           return; }
    if (!form.programa.trim())       { setError('El programa es obligatorio');         return; }
    setError('');
    onGuardar(form);
  };

  const fld = (label, key, type = 'text', placeholder = '') => (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
      <label style={{ fontSize: 12, fontWeight: 600, color: '#374151' }}>{label}</label>
      <input type={type} value={form[key]} onChange={e => campo(key, e.target.value)}
        placeholder={placeholder}
        style={{ padding: '8px 10px', border: '1px solid #d1d5db', borderRadius: 7, fontSize: 13 }} />
    </div>
  );

  return (
    <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.45)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 200 }}>
      <div style={{ background: '#fff', borderRadius: 12, padding: 28, width: 520, display: 'flex', flexDirection: 'column', gap: 14, maxHeight: '90vh', overflowY: 'auto' }}>
        <h3 style={{ fontSize: 17, fontWeight: 700, color: '#1e3a5f', margin: 0 }}>Registrar estudiante</h3>

        {error && <p style={{ background: '#fef2f2', border: '1px solid #fca5a5', borderRadius: 8, padding: '8px 12px', fontSize: 13, color: '#dc2626', margin: 0 }}>⚠ {error}</p>}

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
          {fld('Nombre completo',    'nombre',           'text',   'Ej: Ana García')}
          {fld('Identificación',     'identificacion',   'text',   'Ej: 1001234567')}
          {fld('Correo',             'correo',           'email',  'correo@avh.edu.co')}
          {fld('Teléfono',           'telefono',         'text',   'Ej: 3001234567')}
          {fld('Programa',           'programa',         'text',   'Ej: Ing. Sistemas')}
          {fld('Semestre',           'semestre',         'number', 'Ej: 8')}
          {fld('Créditos aprobados', 'creditosAprobados','number', 'Ej: 120')}
          {fld('Promedio acumulado', 'promedioAcumulado','number', 'Ej: 3.8')}
        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
          <button onClick={onCerrar} style={{ padding: '9px 18px', background: '#f3f4f6', color: '#374151', border: 'none', borderRadius: 8, cursor: 'pointer', fontSize: 13 }}>Cancelar</button>
          <button onClick={guardar}  style={{ padding: '9px 18px', background: '#1e3a5f', color: '#fff', border: 'none', borderRadius: 8, cursor: 'pointer', fontSize: 13, fontWeight: 600 }}>Registrar</button>
        </div>
      </div>
    </div>
  );
}