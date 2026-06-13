import { useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import useAuthStore from '@/store/authStore';
import Layout from '@/shared/components/Layout';
import {
  LoginPage,
  RecuperarPasswordPage,
  RestablecerPasswordPage,
  CambiarPasswordPage,
} from '@/modules/auth';
import { CalificacionesPage } from '@/modules/calificaciones';
import { SeguimientoPage, PracticaDetallePage } from '@/modules/seguimiento';
import { VinculacionPage, VinculacionDetallePage } from '@/modules/vinculacion';
import { CierrePage } from '@/modules/cierre';
import DashboardPage from '@/modules/dashboard/components/DashboardPage';
import UsuariosPage from '@/modules/usuario/components/UsuariosPage';
import FacultadesPage from '@/modules/configuracion/components/FacultadesPage';
import EstudiantesPage from '@/modules/estudiante/components/EstudiantesPage';
import EmpresasPage from '@/modules/empresa/components/EmpresasPage';
import ProgramasPage from '@/modules/configuracion/components/ProgramasPage';
import VacantesPage from '@/modules/vacantes/pages/VacantesPage';
import VacanteDetallePage from '@/modules/vacantes/pages/VacanteDetallePage';
import AsignacionesPage from '@/modules/asignaciones/pages/AsignacionesPage';
import AsignacionDetallePage from '@/modules/asignaciones/pages/AsignacionDetallePage';

function RutaPrivada({ children, roles }) {
  const token = useAuthStore((state) => state.token);
  const rol = useAuthStore((state) => state.rol);

  if (!token) return <Navigate to="/login" replace />;
  if (roles && !roles.includes(rol)) return <Navigate to="/dashboard" replace />;

  return <Layout>{children}</Layout>;
}

function InicializadorSesion({ children }) {
  useEffect(() => {
    useAuthStore.getState().rehidratarDesdeToken();
  }, []);

  return children;
}

export default function AppRouter() {
  return (
    <BrowserRouter>
      <InicializadorSesion>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/recuperar-password" element={<RecuperarPasswordPage />} />
          <Route path="/restablecer-password" element={<RestablecerPasswordPage />} />
          <Route path="/cambiar-password" element={<CambiarPasswordPage />} />

          <Route
            path="/dashboard"
            element={
              <RutaPrivada>
                <DashboardPage />
              </RutaPrivada>
            }
          />

          <Route
            path="/admin/usuarios"
            element={
              <RutaPrivada roles={['ADMIN']}>
                <UsuariosPage />
              </RutaPrivada>
            }
          />

          <Route
            path="/configuracion/facultades"
            element={
              <RutaPrivada roles={['ADMIN', 'COORD_ACADEMICA']}>
                <FacultadesPage />
              </RutaPrivada>
            }
          />

          <Route
            path="/estudiantes"
            element={
              <RutaPrivada roles={['ADMIN', 'COORD_ACADEMICA', 'COORD_PRACTICA', 'SECRETARIA']}>
                <EstudiantesPage />
              </RutaPrivada>
            }
          />

          <Route
            path="/vacantes/:id"
            element={
              <RutaPrivada roles={['ADMIN', 'COORD_PRACTICA', 'SECRETARIA', 'EMPRESA']}>
                <VacanteDetallePage />
              </RutaPrivada>
            }
          />

          <Route
            path="/empresas"
            element={
              <RutaPrivada roles={['ADMIN', 'COORD_PRACTICA', 'SECRETARIA']}>
                <EmpresasPage />
              </RutaPrivada>
            }
          />

          <Route
            path="/configuracion/programas"
            element={
              <RutaPrivada roles={['ADMIN', 'COORD_ACADEMICA']}>
                <ProgramasPage />
              </RutaPrivada>
            }
          />

          <Route
            path="/vacantes"
            element={
              <RutaPrivada roles={['ADMIN', 'COORD_PRACTICA', 'SECRETARIA', 'EMPRESA']}>
                <VacantesPage />
              </RutaPrivada>
            }
          />

          <Route
            path="/asignaciones"
            element={
              <RutaPrivada roles={['COORD_PRACTICA']}>
                <AsignacionesPage />
              </RutaPrivada>
            }
          />

          <Route
            path="/asignaciones/:id"
            element={
              <RutaPrivada roles={['COORD_PRACTICA']}>
                <AsignacionDetallePage />
              </RutaPrivada>
            }
          />

          <Route
            path="/calificaciones/:practicaId"
            element={
              <RutaPrivada
                roles={[
                  'DOCENTE_ASESOR',
                  'TUTOR_EMPRESARIAL',
                  'COORD_PRACTICA',
                  'ESTUDIANTE',
                  'ADMIN',
                ]}
              >
                <CalificacionesPage />
              </RutaPrivada>
            }
          />

          <Route
            path="/seguimiento"
            element={
              <RutaPrivada
                roles={[
                  'COORD_PRACTICA',
                  'DOCENTE_ASESOR',
                  'TUTOR_EMPRESARIAL',
                  'ESTUDIANTE',
                  'ADMIN',
                ]}
              >
                <SeguimientoPage />
              </RutaPrivada>
            }
          />

          <Route
            path="/seguimiento/:id"
            element={
              <RutaPrivada
                roles={[
                  'COORD_PRACTICA',
                  'DOCENTE_ASESOR',
                  'TUTOR_EMPRESARIAL',
                  'ESTUDIANTE',
                  'ADMIN',
                ]}
              >
                <PracticaDetallePage />
              </RutaPrivada>
            }
          />

          <Route
            path="/vinculacion"
            element={
              <RutaPrivada roles={['COORD_PRACTICA', 'TUTOR_EMPRESARIAL', 'ESTUDIANTE', 'ADMIN']}>
                <VinculacionPage />
              </RutaPrivada>
            }
          />

          <Route
            path="/vinculacion/:asignacionId"
            element={
              <RutaPrivada roles={['COORD_PRACTICA', 'TUTOR_EMPRESARIAL', 'ESTUDIANTE', 'ADMIN']}>
                <VinculacionDetallePage />
              </RutaPrivada>
            }
          />

          <Route
            path="/cierre/:practicaId"
            element={
              <RutaPrivada roles={['COORD_PRACTICA', 'ADMIN']}>
                <CierrePage />
              </RutaPrivada>
            }
          />

          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </InicializadorSesion>
    </BrowserRouter>
  );
}
