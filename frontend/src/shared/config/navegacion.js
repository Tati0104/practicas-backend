/**
 * Fuente única de verdad para la navegación lateral (Open/Closed).
 * Para agregar un módulo: declararlo aquí; Sidebar y rutas lo consumen sin duplicar lógica.
 */

export const ROLES = {
  ADMIN: 'ADMIN',
  DIRECCION: 'DIRECCION',
  COORD_ACADEMICA: 'COORD_ACADEMICA',
  COORD_PRACTICA: 'COORD_PRACTICA',
  SECRETARIA: 'SECRETARIA',
  DOCENTE_ASESOR: 'DOCENTE_ASESOR',
  EMPRESA: 'EMPRESA',
  TUTOR_EMPRESARIAL: 'TUTOR_EMPRESARIAL',
  ESTUDIANTE: 'ESTUDIANTE',
};

/** @typedef {{ id: string, nombre: string, ruta: string, icono: string, roles: string[], prefijo?: boolean }} ItemNavegacion */
/** @typedef {{ id: string, nombre: string, items: ItemNavegacion[] }} GrupoNavegacion */

/** @type {GrupoNavegacion[]} */
export const GRUPOS_NAVEGACION = [
  {
    id: 'inicio',
    nombre: 'Inicio',
    items: [
      {
        id: 'dashboard',
        nombre: 'Panel de inicio',
        ruta: '/dashboard',
        icono: 'LayoutDashboard',
        roles: Object.values(ROLES),
      },
    ],
  },
  {
    id: 'sistema',
    nombre: 'Gestión del sistema',
    items: [
      {
        id: 'academica',
        nombre: 'Facultades y programas',
        ruta: '/configuracion/academica',
        icono: 'Building2',
        roles: [ROLES.ADMIN, ROLES.COORD_ACADEMICA],
      },
      {
        id: 'docentes-asesores',
        nombre: 'Docentes Asesores',
        ruta: '/docentes-asesores',
        icono: 'BookOpen',
        roles: [ROLES.ADMIN, ROLES.COORD_ACADEMICA],
      },
    ],
  },
  {
    id: 'usuarios',
    nombre: 'Gestión de usuarios',
    items: [
      {
        id: 'usuarios',
        nombre: 'Usuarios',
        ruta: '/admin/usuarios',
        icono: 'Users',
        roles: [ROLES.ADMIN],
      },
    ],
  },
  {
    id: 'estudiantes',
    nombre: 'Gestión de estudiantes',
    items: [
      {
        id: 'estudiantes',
        nombre: 'Estudiantes',
        ruta: '/estudiantes',
        icono: 'GraduationCap',
        roles: [ROLES.ADMIN, ROLES.COORD_ACADEMICA, ROLES.COORD_PRACTICA, ROLES.SECRETARIA],
      },
    ],
  },
  {
    id: 'empresas',
    nombre: 'Gestión de empresas',
    items: [
      {
        id: 'empresas',
        nombre: 'Empresas',
        ruta: '/empresas',
        icono: 'Briefcase',
        roles: [ROLES.ADMIN, ROLES.COORD_PRACTICA, ROLES.SECRETARIA],
      },
    ],
  },
  {
    id: 'vacantes',
    nombre: 'Vacantes y postulaciones',
    items: [
      {
        id: 'vacantes-postulaciones',
        nombre: 'Vacantes y postulaciones',
        ruta: '/vacantes-postulaciones',
        icono: 'ClipboardList',
        roles: [ROLES.ADMIN, ROLES.COORD_PRACTICA],
      },
      {
        id: 'vacantes',
        nombre: 'Vacantes',
        ruta: '/vacantes',
        icono: 'ClipboardList',
        roles: [ROLES.SECRETARIA, ROLES.EMPRESA],
      },
    ],
  },
  {
    id: 'vinculacion',
    nombre: 'Vinculación',
    items: [
      {
        id: 'vinculacion',
        nombre: 'Documentos',
        ruta: '/vinculacion',
        icono: 'FileText',
        roles: [ROLES.ADMIN, ROLES.COORD_PRACTICA, ROLES.TUTOR_EMPRESARIAL, ROLES.ESTUDIANTE],
        prefijo: true,
      },
    ],
  },
  {
    id: 'seguimiento',
    nombre: 'Seguimiento',
    items: [
      {
        id: 'seguimiento',
        nombre: 'Seguimiento',
        ruta: '/seguimiento',
        icono: 'Activity',
        roles: [
          ROLES.ADMIN,
          ROLES.COORD_PRACTICA,
          ROLES.DOCENTE_ASESOR,
          ROLES.TUTOR_EMPRESARIAL,
          ROLES.ESTUDIANTE,
        ],
        prefijo: true,
      },
    ],
  },
  {
    id: 'calificaciones',
    nombre: 'Calificaciones',
    items: [
      {
        id: 'calificaciones',
        nombre: 'Evaluaciones',
        ruta: '/calificaciones',
        icono: 'Star',
        roles: [
          ROLES.ADMIN,
          ROLES.COORD_PRACTICA,
          ROLES.DOCENTE_ASESOR,
          ROLES.TUTOR_EMPRESARIAL,
          ROLES.ESTUDIANTE,
        ],
        prefijo: true,
      },
    ],
  },
  {
    id: 'cierre',
    nombre: 'Cierre de práctica',
    items: [
      {
        id: 'cierre',
        nombre: 'Checklist de cierre',
        ruta: '/cierre',
        icono: 'CheckSquare',
        roles: [ROLES.ADMIN, ROLES.COORD_PRACTICA],
        prefijo: true,
      },
    ],
  },
  {
    id: 'reportes',
    nombre: 'Reportes',
    items: [
      {
        id: 'reportes',
        nombre: 'Indicadores',
        ruta: '/reportes',
        icono: 'BarChart3',
        roles: [ROLES.ADMIN, ROLES.COORD_PRACTICA, ROLES.DIRECCION],
      },
    ],
  },
  {
    id: 'correo',
    nombre: 'Correo de práctica',
    items: [
      {
        id: 'plantillas',
        nombre: 'Plantillas',
        ruta: '/admin/correo/plantillas',
        icono: 'Mail',
        roles: [ROLES.ADMIN],
      },
    ],
  },
];

/**
 * Filtra grupos e ítems visibles para un rol (Strategy simple por rol).
 * @param {string | null | undefined} rol
 * @returns {GrupoNavegacion[]}
 */
export function obtenerMenuPorRol(rol) {
  if (!rol) return [];

  return GRUPOS_NAVEGACION.map((grupo) => ({
    ...grupo,
    items: grupo.items.filter((item) => item.roles.includes(rol)),
  })).filter((grupo) => grupo.items.length > 0);
}

/**
 * Determina si una ruta del menú está activa (soporta rutas con parámetros).
 * @param {ItemNavegacion} item
 * @param {string} pathname
 */
export function esRutaActiva(item, pathname) {
  if (item.prefijo) {
    return pathname === item.ruta || pathname.startsWith(`${item.ruta}/`);
  }
  if (item.ruta === '/vacantes-postulaciones') {
    return pathname === item.ruta || pathname.startsWith('/asignaciones');
  }
  return pathname === item.ruta;
}
