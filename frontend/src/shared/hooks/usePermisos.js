// src/shared/hooks/usePermisos.js

/**
 * Stub de hook de permisos.
 * En un proyecto real este hook consultaría el backend o el contexto de auth
 * para saber qué acciones permite el usuario actual.
 * Aquí devolvemos `true` para todas las operaciones para que la UI funcione
 * sin necesidad de implementar la lógica completa.
 */
export function usePermisos() {
  return {
    canCreate: true,
    canEdit: true,
    canApprove: true,
    canReject: true,
    canPause: true,
    canClose: true,
  };
}
