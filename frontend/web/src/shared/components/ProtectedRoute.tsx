import { Navigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import type { RolUsuario } from '../types/auth.types';
import type { ReactNode } from 'react';

interface ProtectedRouteProps {
  children: ReactNode;
  rol: RolUsuario;
}

export default function ProtectedRoute({ children, rol }: ProtectedRouteProps) {
  const { usuario, isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    const destino = rol === 'ADMIN' ? '/admin/login' : '/login';
    return <Navigate to={destino} replace />;
  }

  if (usuario?.rol !== rol) {
    const destino = usuario?.rol === 'ADMIN' ? '/admin/propuestas' : '/catalogo';
    return <Navigate to={destino} replace />;
  }

  return <>{children}</>;
}
