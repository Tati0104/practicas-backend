/**
 * Catálogo de preguntas por tipo de encuesta.
 * Si el backend incluye `preguntas` en la respuesta GET, se preferirán esas.
 */
export const PREGUNTAS_POR_TIPO = {
  TUTOR: [
    {
      id: 'satisfaccion_general',
      texto: '¿Qué tan satisfecho está con el desempeño del estudiante en la práctica?',
      tipo: 'ESCALA',
      requerida: true,
    },
    {
      id: 'cumplimiento_objetivos',
      texto: '¿El estudiante cumplió con los objetivos asignados?',
      tipo: 'ESCALA',
      requerida: true,
    },
    {
      id: 'puntualidad',
      texto: '¿Cómo califica la puntualidad y responsabilidad del estudiante?',
      tipo: 'ESCALA',
      requerida: true,
    },
    {
      id: 'recomendacion',
      texto: '¿Recomendaría a este estudiante para futuras prácticas?',
      tipo: 'ESCALA',
      requerida: true,
    },
    {
      id: 'comentarios',
      texto: 'Comentarios adicionales sobre la experiencia con el estudiante',
      tipo: 'ABIERTA',
      requerida: false,
    },
  ],
  ESTUDIANTE: [
    {
      id: 'satisfaccion_practica',
      texto: '¿Qué tan satisfecho está con su experiencia de práctica empresarial?',
      tipo: 'ESCALA',
      requerida: true,
    },
    {
      id: 'acompanamiento_tutor',
      texto: '¿Cómo califica el acompañamiento de su tutor empresarial?',
      tipo: 'ESCALA',
      requerida: true,
    },
    {
      id: 'aplicacion_conocimientos',
      texto: '¿Pudo aplicar los conocimientos adquiridos en su programa?',
      tipo: 'ESCALA',
      requerida: true,
    },
    {
      id: 'recomendacion_empresa',
      texto: '¿Recomendaría esta empresa a otros estudiantes?',
      tipo: 'ESCALA',
      requerida: true,
    },
    {
      id: 'sugerencias',
      texto: 'Sugerencias para mejorar el programa de prácticas',
      tipo: 'ABIERTA',
      requerida: false,
    },
  ],
};

/**
 * @param {string} tipo
 * @param {Array|undefined|null} preguntasBackend
 */
export function resolverPreguntas(tipo, preguntasBackend) {
  if (Array.isArray(preguntasBackend) && preguntasBackend.length > 0) {
    return preguntasBackend;
  }
  return PREGUNTAS_POR_TIPO[tipo] ?? [];
}
