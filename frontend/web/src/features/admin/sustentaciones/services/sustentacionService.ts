import clienteAdmin from '../../../../shared/api/clienteAdmin';
import type { ApiResponse } from '../../../../shared/types/api.types';

export interface IntegranteSustentacionDto {
  nombre: string;
  codigo_estudiante: string;
}

export interface ProyectoSustentacionDto {
  id: number;
  nombre: string;
  descripcion: string;
  ciclo: string;
  grupo_codigo: string;
  grupo_periodo: string;
  estado: string;
  url?: string;
  comentario_evaluacion?: string;
  integrantes?: IntegranteSustentacionDto[];
}

export interface EvaluarRequest {
  estado: 'APROBADO' | 'NO_APROBADO';
  comentario?: string;
}

export async function listarProyectosSustentacion(): Promise<ProyectoSustentacionDto[]> {
  const res = await clienteAdmin.get<ApiResponse<ProyectoSustentacionDto[]>>('/api/admin/proyectos/');
  return res.data.data;
}

export async function obtenerProyectoSustentacion(id: number): Promise<ProyectoSustentacionDto> {
  const res = await clienteAdmin.get<ApiResponse<ProyectoSustentacionDto>>(`/api/admin/proyectos/${id}`);
  return res.data.data;
}

export async function evaluarProyecto(id: number, datos: EvaluarRequest): Promise<void> {
  await clienteAdmin.put(`/api/admin/proyectos/${id}/evaluar`, datos);
}
