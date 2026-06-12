const TOKEN_KEY = 'practicas_token';

/**
 * Decodifica el payload de un JWT sin verificar la firma (solo lectura en cliente).
 * @param {string} token
 * @returns {Record<string, unknown>|null}
 */
export function decodificarToken(token) {
  if (!token || typeof token !== 'string') return null;

  try {
    const partes = token.split('.');
    if (partes.length !== 3) return null;

    const payload = partes[1].replace(/-/g, '+').replace(/_/g, '/');
    const json = decodeURIComponent(
      atob(payload)
        .split('')
        .map((c) => `%${(`00${c.charCodeAt(0).toString(16)}`).slice(-2)}`)
        .join('')
    );

    return JSON.parse(json);
  } catch {
    return null;
  }
}

/**
 * @param {string} token
 * @returns {boolean}
 */
export function tokenExpirado(token) {
  const claims = decodificarToken(token);
  if (!claims?.exp) return true;

  const ahora = Math.floor(Date.now() / 1000);
  return claims.exp <= ahora;
}

/**
 * Extrae datos de sesión del JWT según la convención del backend.
 * @param {string} token
 */
export function extraerDatosSesion(token) {
  const claims = decodificarToken(token);
  if (!claims) return null;

  return {
    correo: claims.sub ?? null,
    rol: claims.rol ?? null,
    scope: claims.scope ?? null,
    facultadId: claims.facultadId ?? null,
    programaId: claims.programaId ?? null,
    nombre: claims.nombre ?? null,
    exp: claims.exp ?? null,
  };
}

export function obtenerTokenAlmacenado() {
  return localStorage.getItem(TOKEN_KEY);
}

export function guardarToken(token) {
  localStorage.setItem(TOKEN_KEY, token);
}

export function eliminarToken() {
  localStorage.removeItem(TOKEN_KEY);
}

export { TOKEN_KEY };
