import { useState, useEffect } from 'react';
import empresaService from '../services/empresaService';

export default function ModalEmpresa({ onGuardar, onCerrar }) {
  const [form,     setForm]     = useState({ nit: '', razonSocial: '', sector: '', direccion: '', municipio: '', telefono: '' });
  const [sectores, setSectores] = useState([]);
  const [error,    setError]    = useState('');

  useEffect(() => {
    empresaService.listarSectores()
      .then(res => setSectores(res.data))
      .catch(() => setSectores([]));
  }, []);

  const campo = (k, v) => setForm(f => ({ ...f, [k]: v }));

  const guardar = () => {
    if (!form.nit.trim())         { setError('El NIT es obligatorio');          return; }
    if (!form.razonSocial.trim()) { setError('La razón social es obligatoria'); return; }
    if (!form.sector)             { setError('El sector es obligatorio');        return; }
    setError('');
    const { sector: sectorId, ...resto } = form;
    onGuardar({ ...resto, sector: { id: Number(sectorId) } });
  };

  const fld = (label, key, placeholder = '') => (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
      <label style={{ fontSize: 12, fontWeight: 600, color: '#374151' }}>{label}</label>
      <input value={form[key]} onChange={e => campo(key, e.target.value)}
        placeholder={placeholder}
        style={{ padding: '8px 10px', border: '1px solid #d1d5db', borderRadius: 7, fontSize: 13 }} />
    </div>
  );

  return (
    <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.45)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 200 }}>
      <div style={{ background: '#fff', borderRadius: 12, padding: 28, width: 480, display: 'flex', flexDirection: 'column', gap: 14 }}>
        <h3 style={{ fontSize: 17, fontWeight: 700, color: '#1e3a5f', margin: 0 }}>Registrar empresa</h3>

        {error && <p style={{ background: '#fef2f2', border: '1px solid #fca5a5', borderRadius: 8, padding: '8px 12px', fontSize: 13, color: '#dc2626', margin: 0 }}>⚠ {error}</p>}

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
          {fld('NIT',          'nit',         'Ej: 900123456-1')}
          {fld('Razón social', 'razonSocial', 'Nombre de la empresa')}
          <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
            <label style={{ fontSize: 12, fontWeight: 600, color: '#374151' }}>Sector</label>
            <select value={form.sector} onChange={e => campo('sector', e.target.value)}
              style={{ padding: '8px 10px', border: '1px solid #d1d5db', borderRadius: 7, fontSize: 13 }}>
              <option value="">Seleccionar</option>
              {sectores.map(s => <option key={s.id} value={s.id}>{s.nombre}</option>)}
            </select>
          </div>
          {fld('Municipio',  'municipio',  'Ej: Armenia')}
          {fld('Dirección',  'direccion',  'Ej: Calle 10 # 5-20')}
          {fld('Teléfono',   'telefono',   'Ej: 3001234567')}
        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
          <button onClick={onCerrar} style={{ padding: '9px 18px', background: '#f3f4f6', color: '#374151', border: 'none', borderRadius: 8, cursor: 'pointer', fontSize: 13 }}>Cancelar</button>
          <button onClick={guardar}  style={{ padding: '9px 18px', background: '#1e3a5f', color: '#fff',    border: 'none', borderRadius: 8, cursor: 'pointer', fontSize: 13, fontWeight: 600 }}>Registrar</button>
        </div>
      </div>
    </div>
  );
}
