// src/modules/vinculacion/components/SubirDocumentoDropzone.jsx

/**
 * Componente: SubirDocumentoDropzone
 * ───────────────────────────────────
 * Zona de arrastre (dropzone) para subir archivos PDF o DOC al servidor.
 *
 * ¿Qué hace?
 *   - Permite al usuario arrastrar un archivo o hacer clic para seleccionarlo.
 *   - Solo acepta archivos PDF (.pdf) y Word (.doc, .docx).
 *   - Valida que el archivo no supere el tamaño máximo (por defecto 10 MB).
 *   - Al seleccionar un archivo válido, muestra su nombre y llama a onArchivoSeleccionado.
 *   - Muestra un botón "Subir" que solo aparece cuando hay un archivo listo.
 *   - Mientras sube (isPending) desactiva todos los controles y muestra "Subiendo...".
 *
 * Props:
 *   titulo            → string, nombre del documento (ej: "Carta de Presentación")
 *   onSubir           → function(archivo: File) — llamada al hacer clic en "Subir"
 *   isPending         → boolean — true mientras la mutación está en curso
 *   deshabilitado     → boolean — true si el doc ya está subido o el usuario no tiene permiso
 *   maxMB             → number (default: 10) — límite de tamaño en megabytes
 */

import { useState, useRef } from 'react';

// Tipos MIME aceptados para PDF y Word
const TIPOS_ACEPTADOS = [
  'application/pdf',
  'application/msword',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
];

// Extensiones legibles para mostrar al usuario
const EXTENSIONES_LEGIBLES = '.pdf, .doc, .docx';

export default function SubirDocumentoDropzone({
  titulo,
  onSubir,
  isPending = false,
  deshabilitado = false,
  maxMB = 10,
}) {
  // Estado del archivo seleccionado (null = ninguno)
  const [archivo, setArchivo] = useState(null);
  // Mensaje de error de validación local
  const [errorLocal, setErrorLocal] = useState('');
  // Controla si el usuario está arrastrando sobre la zona
  const [arrastrando, setArrastrando] = useState(false);

  // Referencia al input type="file" oculto
  const inputRef = useRef(null);

  // Tamaño máximo en bytes
  const maxBytes = maxMB * 1024 * 1024;

  /**
   * Valida y registra el archivo seleccionado.
   * Se llama tanto al arrastrar como al hacer clic y seleccionar.
   */
  const procesarArchivo = (file) => {
    setErrorLocal('');

    // Validar tipo de archivo
    if (!TIPOS_ACEPTADOS.includes(file.type)) {
      setErrorLocal(`Solo se permiten archivos ${EXTENSIONES_LEGIBLES}`);
      return;
    }

    // Validar tamaño
    if (file.size > maxBytes) {
      setErrorLocal(`El archivo supera el límite de ${maxMB} MB`);
      return;
    }

    // Si pasa las validaciones, guardamos el archivo en el estado
    setArchivo(file);
  };

  // ── Handlers de drag & drop ──────────────────────────────────────

  const onDragOver = (e) => {
    e.preventDefault(); // Necesario para que el drop funcione
    setArrastrando(true);
  };

  const onDragLeave = () => setArrastrando(false);

  const onDrop = (e) => {
    e.preventDefault();
    setArrastrando(false);
    const file = e.dataTransfer.files[0];
    if (file) procesarArchivo(file);
  };

  // ── Handler del input file ──────────────────────────────────────

  const onInputChange = (e) => {
    const file = e.target.files[0];
    if (file) procesarArchivo(file);
    // Limpiamos el input para que se pueda volver a seleccionar el mismo archivo
    e.target.value = '';
  };

  // ── Handler del botón "Subir" ───────────────────────────────────

  const handleSubir = () => {
    if (archivo && onSubir) {
      onSubir(archivo);
    }
  };

  // ── Render ───────────────────────────────────────────────────────

  const inactivo = deshabilitado || isPending;

  return (
    <div style={estilos.contenedor}>
      {/* Zona de arrastre */}
      <div
        onClick={() => !inactivo && inputRef.current?.click()}
        onDragOver={!inactivo ? onDragOver : undefined}
        onDragLeave={!inactivo ? onDragLeave : undefined}
        onDrop={!inactivo ? onDrop : undefined}
        style={{
          ...estilos.zona,
          borderColor:   arrastrando ? '#2563eb' : '#d1d5db',
          background:    arrastrando ? '#eff6ff' : inactivo ? '#f9fafb' : '#fff',
          cursor:        inactivo ? 'not-allowed' : 'pointer',
          opacity:       inactivo ? 0.6 : 1,
        }}
      >
        {/* Input oculto — se activa al hacer clic en la zona */}
        <input
          ref={inputRef}
          type="file"
          accept={EXTENSIONES_LEGIBLES}
          onChange={onInputChange}
          style={{ display: 'none' }}
          aria-label={`Seleccionar ${titulo}`}
        />

        {/* Ícono visual */}
        <div style={{ fontSize: 28, marginBottom: 6 }}>📄</div>

        {/* Texto principal */}
        {archivo ? (
          // Muestra el archivo ya seleccionado
          <div style={estilos.archivoSeleccionado}>
            <span style={{ fontWeight: 600 }}>{archivo.name}</span>
            <span style={{ color: '#6b7280', fontSize: 11 }}>
              {(archivo.size / 1024).toFixed(0)} KB
            </span>
          </div>
        ) : (
          // Instrucción para arrastrar o hacer clic
          <div style={estilos.instruccion}>
            <p style={{ margin: 0, fontWeight: 600, color: '#374151' }}>
              Arrastra aquí o haz clic para seleccionar
            </p>
            <p style={{ margin: 0, fontSize: 11, color: '#9ca3af' }}>
              {EXTENSIONES_LEGIBLES} · máx. {maxMB} MB
            </p>
          </div>
        )}
      </div>

      {/* Mensaje de error de validación local */}
      {errorLocal && (
        <p style={estilos.error}>{errorLocal}</p>
      )}

      {/* Botón "Subir" — solo visible cuando hay archivo seleccionado */}
      {archivo && !deshabilitado && (
        <button
          type="button"
          onClick={handleSubir}
          disabled={isPending}
          style={{
            ...estilos.btnSubir,
            opacity: isPending ? 0.7 : 1,
            cursor:  isPending ? 'wait' : 'pointer',
          }}
        >
          {isPending ? 'Subiendo...' : `Subir ${titulo}`}
        </button>
      )}
    </div>
  );
}

// ── Estilos en línea (no Tailwind para evitar dependencia extra) ──────────────

const estilos = {
  contenedor: {
    display: 'flex',
    flexDirection: 'column',
    gap: 8,
  },
  zona: {
    border: '2px dashed',
    borderRadius: 10,
    padding: '20px 16px',
    textAlign: 'center',
    transition: 'border-color 0.2s, background 0.2s',
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
  },
  instruccion: {
    display: 'flex',
    flexDirection: 'column',
    gap: 4,
  },
  archivoSeleccionado: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    gap: 2,
    color: '#1e40af',
    fontSize: 13,
  },
  error: {
    color: '#dc2626',
    fontSize: 12,
    margin: 0,
  },
  btnSubir: {
    padding: '8px 18px',
    background: '#2563eb',
    color: '#fff',
    border: 'none',
    borderRadius: 6,
    fontSize: 13,
    fontWeight: 600,
    alignSelf: 'flex-start',
  },
};
