import { Routes, Route, Navigate } from 'react-router-dom';
import ProtectedRoute from '../shared/components/ProtectedRoute';
import LayoutEstudiante from '../shared/components/LayoutEstudiante';
import LayoutAdmin from '../shared/components/LayoutAdmin';
import { lazy, Suspense } from 'react';

// Auth
const LoginEstudiante = lazy(() => import('../features/auth/pages/LoginEstudiante'));
const RegistroEstudiante = lazy(() => import('../features/auth/pages/RegistroEstudiante'));
const LoginAdmin = lazy(() => import('../features/auth/pages/LoginAdmin'));

// Estudiante
const Inicio = lazy(() => import('../features/inicio/pages/Inicio'));
const CatalogoProyectos = lazy(() => import('../features/catalogo/pages/CatalogoProyectos'));
const DetalleProyecto = lazy(() => import('../features/catalogo/pages/DetalleProyecto'));
const MiGrupo = lazy(() => import('../features/grupos/pages/MiGrupo'));
const MiPropuesta = lazy(() => import('../features/propuestas/pages/MiPropuesta'));
const AsistenteIA = lazy(() => import('../features/asistente/pages/AsistenteIA'));

// Admin
const GestionPropuestasAdmin = lazy(() => import('../features/admin/propuestas/pages/GestionPropuestasAdmin'));
const DetallePropuestaAdmin = lazy(() => import('../features/admin/propuestas/pages/DetallePropuestaAdmin'));
const DashboardAdmin = lazy(() => import('../features/admin/dashboard/pages/DashboardAdmin'));

function Cargando() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-[#EEF2F7]">
      <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-[#2563EB]" />
    </div>
  );
}

export default function AppRouter() {
  return (
    <Suspense fallback={<Cargando />}>
      <Routes>
        {/* Públicas */}
        <Route path="/login" element={<LoginEstudiante />} />
        <Route path="/registro" element={<RegistroEstudiante />} />
        <Route path="/admin/login" element={<LoginAdmin />} />

        {/* Rutas estudiante — sidebar LayoutEstudiante */}
        <Route element={<ProtectedRoute rol="ESTUDIANTE"><LayoutEstudiante /></ProtectedRoute>}>
          <Route path="/inicio" element={<Inicio />} />
          <Route path="/catalogo" element={<CatalogoProyectos />} />
          <Route path="/catalogo/:id" element={<DetalleProyecto />} />
          <Route path="/mi-grupo" element={<MiGrupo />} />
          <Route path="/mi-propuesta" element={<MiPropuesta />} />
          <Route path="/asistente" element={<AsistenteIA />} />
        </Route>

        {/* Rutas admin — sidebar LayoutAdmin */}
        <Route element={<ProtectedRoute rol="ADMIN"><LayoutAdmin /></ProtectedRoute>}>
          <Route path="/admin/propuestas" element={<GestionPropuestasAdmin />} />
          <Route path="/admin/propuestas/:id" element={<DetallePropuestaAdmin />} />
          <Route path="/admin/dashboard" element={<DashboardAdmin />} />
        </Route>

        {/* Redirecciones */}
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </Suspense>
  );
}
