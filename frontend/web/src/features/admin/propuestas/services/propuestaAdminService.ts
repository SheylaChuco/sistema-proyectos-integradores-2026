import clienteAdmin from '../../../../shared/api/clienteAdmin';
import type { ApiResponse } from '../../../../shared/types/api.types';
import type { PropuestaAdminDto, ObservarRequest } from '../../../../shared/types/propuesta.types';

export interface FiltrosPropuestas {
  estado?: string;
}

export async function listarPropuestas(filtros: FiltrosPropuestas = {}): Promise<PropuestaAdminDto[]> {
  const params = new URLSearchParams();
  if (filtros.estado) params.set('estado', filtros.estado);
  const res = await clienteAdmin.get<ApiResponse<PropuestaAdminDto[]>>(`/api/admin/propuestas?${params}`);
  return res.data.data;
}

export async function obtenerPropuesta(id: number): Promise<PropuestaAdminDto> {
  const res = await clienteAdmin.get<ApiResponse<PropuestaAdminDto>>(`/api/admin/propuestas/${id}`);
  return res.data.data;
}

export async function aprobarPropuesta(id: number): Promise<PropuestaAdminDto> {
  const res = await clienteAdmin.put<ApiResponse<PropuestaAdminDto>>(`/api/admin/propuestas/${id}/aprobar`, {});
  return res.data.data;
}

export async function observarPropuesta(id: number, datos: ObservarRequest): Promise<PropuestaAdminDto> {
  const res = await clienteAdmin.put<ApiResponse<PropuestaAdminDto>>(`/api/admin/propuestas/${id}/observar`, datos);
  return res.data.data;
}
