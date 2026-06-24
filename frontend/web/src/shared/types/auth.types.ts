export type RolUsuario = 'ESTUDIANTE' | 'ADMIN';

export interface LoginRequest {
  correo: string;
  password: string;
}

export interface RegistroRequest {
  nombre: string;
  codigo_estudiante: string;
  correo: string;
  password: string;
}

export interface LoginResponse {
  access_token: string;
  usuario: {
    id: number;
    nombre: string;
    correo: string;
    rol: RolUsuario;
    codigo_estudiante?: string;
  };
}
