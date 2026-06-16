/** Catálogo de roles y alcances automáticos para la gestión de usuarios. */

export const ROLES = [
  'ADMIN',
  'DIRECCION',
  'COORD_ACADEMICA',
  'COORD_PRACTICA',
  'SECRETARIA',
  'DOCENTE_ASESOR',
  'EMPRESA',
  'TUTOR_EMPRESARIAL',
  'ESTUDIANTE',
];

/** Roles que deben tener un programa asignado. */
export const ROLES_CON_PROGRAMA = ['DOCENTE_ASESOR', 'ESTUDIANTE'];

/** Roles que deben tener una facultad asignada. */
export const ROLES_CON_FACULTAD = ['COORD_ACADEMICA', 'COORD_PRACTICA', 'SECRETARIA'];

/** Roles que deben estar vinculados a una empresa. */
export const ROLES_CON_EMPRESA = ['TUTOR_EMPRESARIAL'];

const ETIQUETAS_ROL = {
  ADMIN: 'Administrador',
  DIRECCION: 'Dirección',
  COORD_ACADEMICA: 'Coordinación académica',
  COORD_PRACTICA: 'Coordinación de prácticas',
  SECRETARIA: 'Secretaría',
  DOCENTE_ASESOR: 'Docente asesor',
  EMPRESA: 'Empresa',
  TUTOR_EMPRESARIAL: 'Tutor empresarial',
  ESTUDIANTE: 'Estudiante',
};

/** Alcance que el sistema asigna automáticamente según el rol. */
export const SCOPE_POR_ROL = {
  ADMIN: 'GLOBAL',
  DIRECCION: 'GLOBAL',
  COORD_ACADEMICA: 'FACULTAD',
  COORD_PRACTICA: 'FACULTAD',
  SECRETARIA: 'FACULTAD',
  DOCENTE_ASESOR: 'PROGRAMA',
  EMPRESA: 'ASIGNADO',
  TUTOR_EMPRESARIAL: 'ASIGNADO',
  ESTUDIANTE: 'PROGRAMA',
};

export function etiquetaRol(rol) {
  return ETIQUETAS_ROL[rol] ?? rol;
}

export function scopePorRol(rol) {
  return SCOPE_POR_ROL[rol] ?? null;
}

export function requiereFacultad(rol) {
  return ROLES_CON_FACULTAD.includes(rol);
}

export function requierePrograma(rol) {
  return ROLES_CON_PROGRAMA.includes(rol);
}

export function requiereIdentificacion(rol) {
  return rol === 'ESTUDIANTE';
}

export function requiereEmpresa(rol) {
  return ROLES_CON_EMPRESA.includes(rol);
}

export function opcionesRol(incluirTodos = false) {
  const opciones = ROLES.map((value) => ({ value, label: etiquetaRol(value) }));
  return incluirTodos ? [{ value: '', label: 'Todos los roles' }, ...opciones] : opciones;
}

export function dtoUsuario({
  nombre,
  correo,
  rol,
  facultadId,
  empresaId,
  cargoTutor,
  telefonoTutor,
  programaId,
  identificacion,
  telefono,
}) {
  const dto = {
    nombre: nombre.trim(),
    correo: correo.trim(),
    rol,
    scope: scopePorRol(rol),
  };

  if (requiereFacultad(rol)) {
    dto.facultadId = facultadId ? Number(facultadId) : null;
  }

  if (requierePrograma(rol)) {
    dto.programaId = programaId ? Number(programaId) : null;
  }

  if (requiereIdentificacion(rol)) {
    dto.identificacion = identificacion?.trim() || '';
  }

  if (requierePrograma(rol) || requiereIdentificacion(rol)) {
    dto.telefono = telefono?.trim() || null;
  }

  if (requiereEmpresa(rol)) {
    dto.empresaId = empresaId ? Number(empresaId) : null;
    dto.cargoTutor = cargoTutor?.trim() || 'Tutor empresarial';
    dto.telefonoTutor = telefonoTutor?.trim() || '';
  }

  return dto;
}
