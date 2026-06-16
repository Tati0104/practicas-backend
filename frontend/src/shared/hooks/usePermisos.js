import useAuthStore from '@/store/authStore';

const PERMISOS_COMPLETOS = {
  canCreate: true,
  canEdit: true,
  canApprove: true,
  canReject: true,
  canPause: true,
  canClose: true,
  canUploadDocumentos: true,
};

const PERMISOS_POR_ROL = {
  TUTOR_EMPRESARIAL: {
    canCreate: false,
    canEdit: false,
    canApprove: false,
    canReject: false,
    canPause: false,
    canClose: false,
    canUploadDocumentos: false,
  },
  ESTUDIANTE: {
    canCreate: false,
    canEdit: false,
    canApprove: false,
    canReject: false,
    canPause: false,
    canClose: false,
    canUploadDocumentos: true,
  },
  DOCENTE_ASESOR: {
    canCreate: false,
    canEdit: true,
    canApprove: false,
    canReject: false,
    canPause: false,
    canClose: false,
    canUploadDocumentos: false,
  },
};

export function usePermisos() {
  const rol = useAuthStore((state) => state.rol);
  const usuario = useAuthStore((state) => state.usuario);

  return {
    ...(PERMISOS_POR_ROL[rol] ?? PERMISOS_COMPLETOS),
    rol,
    usuario,
  };
}
