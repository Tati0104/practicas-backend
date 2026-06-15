/**
 * Datos mock centralizados.
 * Solo se usan cuando VITE_USE_MOCKS=true (ver shared/config/dataSource.js).
 */

export const MOCK_ESTUDIANTES = [
  { id: 1, nombre: 'Ana García', identificacion: '1001234567', correo: 'ana@avh.edu.co', programa: 'Ing. Sistemas', semestre: 8, estadoAptitud: 'APTO', activo: true },
  { id: 2, nombre: 'Luis Martínez', identificacion: '1002345678', correo: 'luis@avh.edu.co', programa: 'Ing. Civil', semestre: 7, estadoAptitud: 'SIN_EVALUAR', activo: true },
  { id: 3, nombre: 'María López', identificacion: '1003456789', correo: 'maria@avh.edu.co', programa: 'Ing. Sistemas', semestre: 9, estadoAptitud: 'NO_APTO', activo: true },
  { id: 4, nombre: 'Carlos Ruiz', identificacion: '1004567890', correo: 'carlos@avh.edu.co', programa: 'Administración', semestre: 8, estadoAptitud: 'APTO', activo: false },
];

export const MOCK_EMPRESAS = [
  { id: 1, nit: '900123456-1', razonSocial: 'Tech Solutions SAS', sector: 'TECNOLOGIA', municipio: 'Armenia', activo: true },
  { id: 2, nit: '800234567-2', razonSocial: 'Constructora ABC', sector: 'CONSTRUCCION', municipio: 'Calarcá', activo: true },
  { id: 3, nit: '700345678-3', razonSocial: 'Agro Valle Ltda', sector: 'AGRICULTURA', municipio: 'Montenegro', activo: false },
];

export const MOCK_USUARIOS = [
  { id: 1, nombre: 'Admin AVH', correo: 'admin@avh.edu.co', rol: 'ADMIN', scope: 'GLOBAL', activo: true },
  { id: 2, nombre: 'Coord Práctica', correo: 'coord@avh.edu.co', rol: 'COORD_PRACTICA', scope: 'PROGRAMA', activo: true },
  { id: 3, nombre: 'Coord Académica', correo: 'academica@avh.edu.co', rol: 'COORD_ACADEMICA', scope: 'FACULTAD', activo: true },
  { id: 4, nombre: 'Docente Asesor', correo: 'docente@avh.edu.co', rol: 'DOCENTE_ASESOR', scope: 'ASIGNADO', activo: false },
];

export const MOCK_VACANTES = [
  { id: 1, empresa: 'Tech Solutions SAS', cargo: 'Desarrollador Backend', modalidad: 'PRESENCIAL', cuposTotal: 2, cuposDisponibles: 1, estado: 'ACTIVA' },
  { id: 2, empresa: 'Constructora ABC', cargo: 'Ing. Residente de Obra', modalidad: 'PRESENCIAL', cuposTotal: 1, cuposDisponibles: 1, estado: 'PENDIENTE_APROBACION' },
  { id: 3, empresa: 'Tech Solutions SAS', cargo: 'Analista de Datos', modalidad: 'REMOTO', cuposTotal: 3, cuposDisponibles: 0, estado: 'CUPOS_COMPLETOS' },
];

export const MOCK_ASIGNACIONES = [
  { id: 1, estudiante: { nombre: 'Ana García', codigo: '2021001', programa: 'Ing. Sistemas' }, vacante: { cargo: 'Desarrollador Frontend', empresa: 'Tech Solutions SAS' }, fechaAsignacion: '2024-02-10', estado: 'ASIGNADA' },
  { id: 2, estudiante: { nombre: 'Carlos Ruiz', codigo: '2021002', programa: 'Ing. Industrial' }, vacante: { cargo: 'Analista de Procesos', empresa: 'Constructora ABC' }, fechaAsignacion: '2024-02-15', estado: 'EN_VINCULACION' },
  { id: 3, estudiante: { nombre: 'María López', codigo: '2020003', programa: 'Administración' }, vacante: { cargo: 'Asistente Administrativo', empresa: 'Logística Express' }, fechaAsignacion: '2024-01-20', estado: 'CANCELADA' },
];

export const MOCK_PRACTICAS = [
  {
    id: 1,
    estudiante: 'Ana Gómez',
    empresa: 'Tech Solutions SA',
    docente: 'Prof. Martínez',
    corte: 1,
    estadoSeguimiento: 'AL_DIA',
    fechaUltimaActividad: '2026-06-14T10:00:00',
  },
  {
    id: 2,
    estudiante: 'Carlos Ruiz',
    empresa: 'Innovatech',
    docente: 'Prof. Rodríguez',
    corte: 2,
    estadoSeguimiento: 'PENDIENTE',
    fechaUltimaActividad: '2026-06-05T09:30:00',
  },
  {
    id: 3,
    estudiante: 'Lucía Méndez',
    empresa: 'Global Corp',
    docente: 'Prof. López',
    corte: 1,
    estadoSeguimiento: 'EN_ALERTA',
    fechaUltimaActividad: '2026-05-20T14:15:00',
  },
];

export const MOCK_VINCULACIONES = [
  {
    practicaId: 101,
    asignacionId: 1,
    estudiante: { nombre: 'Ana García', codigo: '2021001', programa: 'Ing. Sistemas' },
    vacante: { cargo: 'Desarrollador Frontend', empresa: 'Tech Solutions SAS' },
    documentos: [
      { id: 1, tipo: 'CARTA', nombre: 'Carta_AnaGarcia.pdf', estado: 'SUBIDO', firmas: [{ tipoFirmante: 'COORDINADOR', firmado: true, fechaFirma: '2024-03-01' }, { tipoFirmante: 'TUTOR', firmado: false, fechaFirma: null }, { tipoFirmante: 'ESTUDIANTE', firmado: false, fechaFirma: null }] },
      { id: 2, tipo: 'CONVENIO', nombre: null, estado: 'PENDIENTE', firmas: [{ tipoFirmante: 'COORDINADOR', firmado: false, fechaFirma: null }, { tipoFirmante: 'TUTOR', firmado: false, fechaFirma: null }, { tipoFirmante: 'ESTUDIANTE', firmado: false, fechaFirma: null }] },
    ],
  },
  {
    practicaId: 102,
    asignacionId: 2,
    estudiante: { nombre: 'Carlos Ruiz', codigo: '2021002', programa: 'Ing. Industrial' },
    vacante: { cargo: 'Analista de Procesos', empresa: 'Constructora ABC' },
    documentos: [
      { id: 3, tipo: 'CARTA', nombre: 'Carta_CarlosRuiz.pdf', estado: 'FIRMADO', firmas: [{ tipoFirmante: 'COORDINADOR', firmado: true, fechaFirma: '2024-03-05' }, { tipoFirmante: 'TUTOR', firmado: true, fechaFirma: '2024-03-06' }, { tipoFirmante: 'ESTUDIANTE', firmado: true, fechaFirma: '2024-03-07' }] },
      { id: 4, tipo: 'CONVENIO', nombre: 'Convenio_CarlosRuiz.pdf', estado: 'SUBIDO', firmas: [{ tipoFirmante: 'COORDINADOR', firmado: true, fechaFirma: '2024-03-08' }, { tipoFirmante: 'TUTOR', firmado: false, fechaFirma: null }, { tipoFirmante: 'ESTUDIANTE', firmado: false, fechaFirma: null }] },
    ],
  },
];

export const MOCK_ALERTAS = [
  { id: 1, practicaId: 3, mensaje: 'Sin actividad registrada en los últimos 15 días', leida: false, fecha: '2024-03-01' },
  { id: 2, practicaId: 3, mensaje: 'Bitácora pendiente de la semana 4', leida: false, fecha: '2024-03-05' },
  { id: 3, practicaId: 2, mensaje: 'Porcentaje de avance inferior al esperado', leida: true, fecha: '2024-02-28' },
];

export const MOCK_DOCUMENTOS = [
  { id: 1, tipo: 'CARTA', nombre: 'Carta_presentacion_AnaGarcia.pdf', estado: 'SUBIDO', firmas: [{ tipoFirmante: 'COORDINADOR', firmado: true, fechaFirma: '2024-03-01' }, { tipoFirmante: 'TUTOR', firmado: false, fechaFirma: null }, { tipoFirmante: 'ESTUDIANTE', firmado: false, fechaFirma: null }] },
  { id: 2, tipo: 'CONVENIO', nombre: null, estado: 'PENDIENTE', firmas: [{ tipoFirmante: 'COORDINADOR', firmado: false, fechaFirma: null }, { tipoFirmante: 'TUTOR', firmado: false, fechaFirma: null }, { tipoFirmante: 'ESTUDIANTE', firmado: false, fechaFirma: null }] },
];

export const MOCK_FACULTADES = [
  { id: 1, nombre: 'Facultad de Ingeniería', activo: true },
  { id: 2, nombre: 'Facultad de Ciencias', activo: true },
  { id: 3, nombre: 'Facultad de Económicas', activo: false },
];

export const MOCK_PROGRAMAS = [
  { id: 1, nombre: 'Ingeniería de Sistemas', facultad: 'Facultad de Ingeniería', activo: true },
  { id: 2, nombre: 'Ingeniería Civil', facultad: 'Facultad de Ingeniería', activo: true },
  { id: 3, nombre: 'Administración', facultad: 'Facultad de Económicas', activo: false },
];

export const MOCK_DETALLE_PRACTICA = {
  id: 1,
  estudiante: { nombre: 'Ana García', codigo: '2021001', programa: 'Ing. Sistemas' },
  empresa: 'Tech Solutions SAS',
  cargo: 'Desarrollador Frontend',
  docente: 'Prof. Martínez',
  tutor: 'Ing. Ramírez',
  estado: 'AL_DIA',
  fechaInicio: '2024-02-01',
  fechaFin: '2024-07-31',
  porcentajeAvance: 75,
  timeline: [
    { id: 1, tipo: 'OBSERVACION', autor: 'Prof. Martínez', fecha: '2024-03-10', contenido: 'El estudiante muestra buen desempeño en las tareas asignadas.' },
    { id: 2, tipo: 'AVANCE_TUTOR', autor: 'Ing. Ramírez', fecha: '2024-03-08', contenido: 'Avance del 75%. Completó módulo de autenticación.', porcentaje: 75 },
  ],
};
