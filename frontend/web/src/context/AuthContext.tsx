import { createContext, useContext, useState, useCallback, type ReactNode } from 'react';
import type { RolUsuario } from '../shared/types/auth.types';

interface UsuarioSesion {
  id: number;
  nombre: string;
  correo: string;
  rol: RolUsuario;
  codigo_estudiante?: string;
}

interface AuthContextValue {
  usuario: UsuarioSesion | null;
  isAuthenticated: boolean;
  loginEstudiante: (token: string, usuario: UsuarioSesion) => void;
  loginAdmin: (token: string, usuario: UsuarioSesion) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

function recuperarSesion(): { usuario: UsuarioSesion | null } {
  try {
    const raw = localStorage.getItem('usuario_sesion');
    if (raw) return { usuario: JSON.parse(raw) };
  } catch {
    // sesión corrupta, ignorar
  }
  return { usuario: null };
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<UsuarioSesion | null>(recuperarSesion().usuario);

  const loginEstudiante = useCallback((token: string, usr: UsuarioSesion) => {
    localStorage.setItem('token_estudiante', token);
    localStorage.setItem('usuario_sesion', JSON.stringify(usr));
    setUsuario(usr);
  }, []);

  const loginAdmin = useCallback((token: string, usr: UsuarioSesion) => {
    localStorage.setItem('token_admin', token);
    localStorage.setItem('usuario_sesion', JSON.stringify(usr));
    setUsuario(usr);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('token_estudiante');
    localStorage.removeItem('token_admin');
    localStorage.removeItem('usuario_sesion');
    setUsuario(null);
  }, []);

  return (
    <AuthContext.Provider value={{ usuario, isAuthenticated: !!usuario, loginEstudiante, loginAdmin, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth debe usarse dentro de AuthProvider');
  return ctx;
}
