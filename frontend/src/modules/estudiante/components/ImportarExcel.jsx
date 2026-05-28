import { useState, useRef } from 'react';

export default function ImportarExcel({ onImportar, onCerrar }) {
  const [archivo,    setArchivo]    = useState(null);
  const [resultado,  setResultado]  = useState(null);
  const [cargando,   setCargando]   = useState(false);
  const inputRef = useRef();

  const seleccionar = (e) => {
    const file = e.target.files[0];
    if (file && file.name.endsWith('.xlsx')) {
      setArchivo(file);
      setResultado(null);
    } else {
      alert('Solo se permiten archivos .xlsx');
    }
  };

  const importar = async () => {
    if (!archivo) return;
    setCargando(true);
    // Mock resultado mientras no hay backend
    await new Promise(r => setTimeout(r, 1200));
    setResultado({ exitosos: 15, errores: 2, total: 17,
      detalleErrores: [
        { fila: 5,  descripcion: 'Correo duplicado' },
        { fila: 12, descripcion: 'Identificación ya registrada' }
      ]
    });
    setCargando(false);
  };

  return (
    <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.45)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 200 }}>
      <div style={{ background: '#fff', borderRadius: 12, padding: 28, width: 480, display: 'flex', flexDirection: 'column', gap: 16 }}>
        <h3 style={{ fontSize: 17, fontWeight: 700, color: '#1e3a5f', margin: 0 }}>Importar estudiantes desde Excel</h3>

        {/* Zona de carga */}
        <div
          onClick={() => inputRef.current.click()}
          style={{ border: '2px dashed #d1d5db', borderRadius: 10, padding: '28px 20px',
                   textAlign: 'center', cursor: 'pointer', background: archivo ? '#f0fdf4' : '#fafafa' }}
        >
          <p style={{ margin: 0, fontSize: 14, color: archivo ? '#059669' : '#6b7280' }}>
            {archivo ? `✓ ${archivo.name}` : '📂 Clic para seleccionar archivo .xlsx'}
          </p>
        </div>
        <input ref={inputRef} type="file" accept=".xlsx" onChange={seleccionar} style={{ display: 'none' }} />

        {/* Resultado */}
        {resultado && (
          <div style={{ background: '#f8fafc', borderRadius: 8, padding: 14 }}>
            <p style={{ margin: '0 0 8px', fontSize: 13, fontWeight: 600, color: '#374151' }}>
              Resultado: {resultado.exitosos}/{resultado.total} importados correctamente
            </p>
            {resultado.detalleErrores.length > 0 && (
              <div>
                <p style={{ margin: '0 0 4px', fontSize: 12, color: '#dc2626', fontWeight: 600 }}>Errores:</p>
                {resultado.detalleErrores.map((e, i) => (
                  <p key={i} style={{ margin: '2px 0', fontSize: 12, color: '#dc2626' }}>
                    Fila {e.fila}: {e.descripcion}
                  </p>
                ))}
              </div>
            )}
          </div>
        )}

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
          <button onClick={onCerrar} style={{ padding: '9px 18px', background: '#f3f4f6', color: '#374151', border: 'none', borderRadius: 8, cursor: 'pointer', fontSize: 13 }}>
            Cerrar
          </button>
          {!resultado && (
            <button onClick={importar} disabled={!archivo || cargando}
              style={{ padding: '9px 18px', background: archivo ? '#1e3a5f' : '#9ca3af', color: '#fff', border: 'none', borderRadius: 8, cursor: archivo ? 'pointer' : 'not-allowed', fontSize: 13, fontWeight: 600 }}>
              {cargando ? 'Importando...' : 'Importar'}
            </button>
          )}
        </div>
      </div>
    </div>
  );
}