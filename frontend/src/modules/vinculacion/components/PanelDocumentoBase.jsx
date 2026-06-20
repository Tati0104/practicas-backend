import { useState } from 'react';
import BadgeDocumento from './BadgeDocumento';
import SubirDocumentoDropzone from './SubirDocumentoDropzone';
import estudianteService from '../../estudiante/services/estudianteService';

const TITULOS = {
  HOJA_DE_VIDA: 'Hoja de Vida',
  PAZ_Y_SALVO: 'Paz y Salvo',
};

export default function PanelDocumentoBase({ tipo, estudiante, puedeSubir, onSubidoExitosamente }) {
  const [isPendingSubir, setIsPendingSubir] = useState(false);

  const documentoCargado = estudiante?.documentos?.find(d => d.tipo === tipo);
  const tieneArchivo = !!documentoCargado;

  const handleSubir = async (archivo) => {
    if (!estudiante) return;
    setIsPendingSubir(true);
    try {
      const formData = new FormData();
      formData.append('tipo', tipo);
      formData.append('archivo', archivo);
      await estudianteService.subirDocumento(estudiante.id, formData);
      onSubidoExitosamente();
    } catch (error) {
      console.error("Error al subir documento base:", error);
    } finally {
      setIsPendingSubir(false);
    }
  };

  const handleDescargar = () => {
    if (!documentoCargado?.url) return;
    const base = import.meta.env.VITE_API_URL || 'http://localhost:8080';
    const url = documentoCargado.url.startsWith('http')
      ? documentoCargado.url
      : `${base}/${documentoCargado.url.replace(/^\/+/, '')}`;
    window.open(url, '_blank', 'noopener,noreferrer');
  };

  return (
    <div style={estilos.panel}>
      <div style={estilos.encabezado}>
        <h3 style={estilos.titulo}>{TITULOS[tipo] || tipo}</h3>
        <BadgeDocumento estado={tieneArchivo ? 'SUBIDO' : 'PENDIENTE'} />
      </div>

      {tieneArchivo && documentoCargado.nombre && (
        <p style={estilos.nombreArchivo}>📎 {documentoCargado.nombre}</p>
      )}

      {tieneArchivo && (
        <button
          type="button"
          onClick={handleDescargar}
          style={estilos.btnDescargar}
        >
          ⬇ Descargar
        </button>
      )}

      {puedeSubir && !tieneArchivo && (
        <SubirDocumentoDropzone
          titulo={TITULOS[tipo]}
          onSubir={handleSubir}
          isPending={isPendingSubir}
          deshabilitado={tieneArchivo}
        />
      )}
    </div>
  );
}

const estilos = {
  panel: {
    background: '#ffffff', border: '1px solid #e5e7eb', borderRadius: 10,
    padding: 18, display: 'flex', flexDirection: 'column', gap: 12,
    boxShadow: '0 1px 4px rgba(0,0,0,0.06)',
  },
  encabezado: {
    display: 'flex', justifyContent: 'space-between', alignItems: 'center',
  },
  titulo: {
    fontSize: 15, fontWeight: 700, color: '#111827', margin: 0,
  },
  nombreArchivo: {
    fontSize: 12, color: '#4b5563', margin: 0, wordBreak: 'break-all',
  },
  btnDescargar: {
    padding: '6px 14px', background: '#eff6ff', color: '#1d4ed8',
    border: '1px solid #bfdbfe', borderRadius: 6, fontSize: 12,
    fontWeight: 600, cursor: 'pointer', alignSelf: 'flex-start',
  },
};
