import clienteEstudiante from '../../../shared/api/clienteEstudiante';
import clienteAdmin from '../../../shared/api/clienteAdmin';
import type { ApiResponse } from '../../../shared/types/api.types';
import type { LoginRequest, RegistroRequest, LoginResponse } from '../../../shared/types/auth.types';

export async function loginEstudiante(datos: LoginRequest): Promise<LoginResponse> {
  const res = await clienteEstudiante.post<ApiResponse<LoginResponse>>('/api/auth/login', datos);
  return res.data.data;
}

export async function registrarEstudiante(datos: RegistroRequest): Promise<LoginResponse> {
  const res = await clienteEstudiante.post<ApiResponse<LoginResponse>>('/api/auth/registro', datos);
  return res.data.data;
}

export async function loginAdmin(datos: LoginRequest): Promise<LoginResponse> {
  const res = await clienteAdmin.post<ApiResponse<LoginResponse>>('/api/admin/auth/login', datos);
  return res.data.data;
}
