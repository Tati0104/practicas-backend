import { useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import useAuthStore from '@/store/authStore';
import Layout from '@/shared/components/Layout';
import ThemeSync from '@/shared/components/ThemeSync';
import {
  LoginPage,
  RecuperarPasswordPage,
  RestablecerPasswordPage,
  CambiarPasswordPage,
} from '@/modules/auth';
import { CalificacionesPage, CalificacionesListPage } from '@/modules/calificaciones';
import { SeguimientoPage, PracticaDetallePage } from '@/modules/seguimiento';
import { VinculacionPage, VinculacionDetallePage } from '@/modules/vinculacion';
import { CierrePage, CierreListPage } from '@/modules/cierre';
import { ReportesPage } from '@/modules/reportes';
import { PlantillasCorreoPage } from '@/modules/correo';
import DashboardPage from '@/modules/dashboard/components/DashboardPage';
import UsuariosPage from '@/modules/usuario/components/UsuariosPage';
import FacultadesProgramasPage from '@/modules/configuracion/components/FacultadesProgramasPage';
import DocentesAsesoresPage from '@/modules/docentes/pages/DocentesAsesoresPage';
import EstudiantesPage from '@/modules/estudiante/components/EstudiantesPage';
import EmpresasPage from '@/modules/empresa/components/EmpresasPage';
import VacantesPage from '@/modules/vacantes/pages/VacantesPage';
import VacanteDetallePage from '@/modules/vacantes/pages/VacanteDetallePage';
import VacantesYAsignacionesPage from '@/modules/vacantes/pages/VacantesYAsignacionesPage';
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
      <ThemeSync />
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
            path="/configuracion/academica"
            element={
              <RutaPrivada roles={['ADMIN']}>
                <FacultadesProgramasPage />
              </RutaPrivada>
            }
          />
          <Route path="/configuracion/facultades" element={<Navigate to="/configuracion/academica" replace />} />
          <Route path="/configuracion/programas" element={<Navigate to="/configuracion/academica" replace />} />

          <Route
            path="/docentes-asesores"
            element={
              <RutaPrivada roles={['ADMIN', 'COORD_ACADEMICA']}>
                <DocentesAsesoresPage />
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
            path="/vacantes"
            element={
              <RutaPrivada roles={['ADMIN', 'COORD_PRACTICA', 'SECRETARIA', 'EMPRESA', 'TUTOR_EMPRESARIAL']}>
                <VacantesPage />
              </RutaPrivada>
            }
          />

          <Route
            path="/vacantes-postulaciones"
            element={
              <RutaPrivada roles={['ADMIN', 'COORD_PRACTICA']}>
                <VacantesYAsignacionesPage />
              </RutaPrivada>
            }
          />

          <Route
            path="/asignaciones"
            element={
              <RutaPrivada roles={['ADMIN', 'COORD_PRACTICA', 'COORD_ACADEMICA']}>
                <AsignacionesPage />
              </RutaPrivada>
            }
          />

          <Route
            path="/asignaciones/:id"
            element={
              <RutaPrivada roles={['ADMIN', 'COORD_PRACTICA', 'COORD_ACADEMICA']}>
                <AsignacionDetallePage />
              </RutaPrivada>
            }
          />

          <Route
            path="/calificaciones"
            element={
              <RutaPrivada
                roles={[
                  'DOCENTE_ASESOR',
                  'TUTOR_EMPRESARIAL',
                  'COORD_PRACTICA',
                  'COORD_ACADEMICA',
                  'ESTUDIANTE',
                  'ADMIN',
                ]}
              >
                <CalificacionesListPage />
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
                  'COORD_ACADEMICA',
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
                  'COORD_ACADEMICA',
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
                  'COORD_ACADEMICA',
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
            path="/vinculacion/practica/:practicaId"
            element={
              <RutaPrivada roles={['COORD_PRACTICA', 'TUTOR_EMPRESARIAL', 'ESTUDIANTE', 'ADMIN']}>
                <VinculacionDetallePage modo="practica" />
              </RutaPrivada>
            }
          />

          <Route
            path="/vinculacion/:asignacionId"
            element={
              <RutaPrivada roles={['COORD_PRACTICA', 'TUTOR_EMPRESARIAL', 'ESTUDIANTE', 'ADMIN']}>
                <VinculacionDetallePage modo="asignacion" />
              </RutaPrivada>
            }
          />

          <Route
            path="/cierre"
            element={
              <RutaPrivada roles={['COORD_PRACTICA', 'SECRETARIA', 'ADMIN']}>
                <CierreListPage />
              </RutaPrivada>
            }
          />

          <Route
            path="/cierre/:practicaId"
            element={
              <RutaPrivada roles={['COORD_PRACTICA', 'SECRETARIA', 'ADMIN']}>
                <CierrePage />
              </RutaPrivada>
            }
          />

          <Route
            path="/reportes"
            element={
              <RutaPrivada roles={['ADMIN', 'COORD_PRACTICA', 'DIRECCION']}>
                <ReportesPage />
              </RutaPrivada>
            }
          />

          <Route
            path="/admin/correo/plantillas"
            element={
              <RutaPrivada roles={['ADMIN']}>
                <PlantillasCorreoPage />
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
