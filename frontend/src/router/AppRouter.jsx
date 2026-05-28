import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import useAuth    from '../shared/hooks/useAuth';
import Layout     from '../shared/components/Layout';
import LoginPage       from '../modules/auth/components/LoginPage';
import DashboardPage   from '../modules/dashboard/components/DashboardPage';
import UsuariosPage    from '../modules/usuario/components/UsuariosPage';
import FacultadesPage  from '../modules/configuracion/components/FacultadesPage';
import EstudiantesPage from '../modules/estudiante/components/EstudiantesPage';
import EmpresasPage    from '../modules/empresa/components/EmpresasPage';

function RutaPrivada({ children, roles }) {
  const { token, usuario } = useAuth();
  if (!token) return <Navigate to="/login" replace />;
  if (roles && !roles.includes(usuario?.rol)) return <Navigate to="/dashboard" replace />;
  return <Layout>{children}</Layout>;
}

export default function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />

        <Route path="/dashboard" element={
          <RutaPrivada><DashboardPage /></RutaPrivada>
        } />

        <Route path="/admin/usuarios" element={
          <RutaPrivada roles={['ADMIN']}><UsuariosPage /></RutaPrivada>
        } />

        <Route path="/configuracion/facultades" element={
          <RutaPrivada roles={['ADMIN', 'COORD_ACADEMICA']}><FacultadesPage /></RutaPrivada>
        } />

        <Route path="/estudiantes" element={
          <RutaPrivada roles={['ADMIN','COORD_ACADEMICA','COORD_PRACTICA','SECRETARIA']}>
            <EstudiantesPage />
          </RutaPrivada>
        } />

        <Route path="/empresas" element={
          <RutaPrivada roles={['ADMIN','COORD_PRACTICA','SECRETARIA']}>
            <EmpresasPage />
          </RutaPrivada>
        } />

        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </BrowserRouter>
  );
}