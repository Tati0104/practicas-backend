const CLAVE_ULTIMA_ACTIVIDAD = 'practicas_ultima_actividad';
const MINUTOS_INACTIVIDAD_DEFAULT = 30;

function minutosInactividad() {
  const raw = import.meta.env.VITE_SESSION_INACTIVITY_MINUTES;
  const parsed = Number(raw);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : MINUTOS_INACTIVIDAD_DEFAULT;
}

export function msInactividadPermitida() {
  return minutosInactividad() * 60 * 1000;
}

export function registrarActividad() {
  localStorage.setItem(CLAVE_ULTIMA_ACTIVIDAD, String(Date.now()));
}

export function sesionInactivaExpirada() {
  const raw = localStorage.getItem(CLAVE_ULTIMA_ACTIVIDAD);
  if (!raw) return false;

  const ultima = Number(raw);
  if (!Number.isFinite(ultima)) return true;

  return Date.now() - ultima > msInactividadPermitida();
}

export function limpiarActividadSesion() {
  localStorage.removeItem(CLAVE_ULTIMA_ACTIVIDAD);
}
