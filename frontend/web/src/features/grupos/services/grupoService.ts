import clienteEstudiante from '../../../shared/api/clienteEstudiante';
import type { ApiResponse } from '../../../shared/types/api.types';

export interface GrupoDto {
  id: number;
  codigo_grupo: string;
  periodo: string;
  integrantes: IntegranteGrupoDto[];
}

export interface IntegranteGrupoDto {
  id: number;
  nombre: string;
  codigo_estudiante: string;
  es_lider: boolean;
}

export interface BuscarEstudianteDto {
  id: number;
  nombre: string;
  codigo_estudiante: string;
  tiene_grupo: boolean;
}

export interface RegistrarGrupoRequest {
  integrante_ids: number[];
}

export async function obtenerMiGrupo(): Promise<GrupoDto | null> {
  try {
    const res = await clienteEstudiante.get<ApiResponse<GrupoDto>>('/api/grupos/mi-grupo');
    return res.data.data;
  } catch {
    return null;
  }
}

export async function buscarEstudiantes(nombre: string): Promise<BuscarEstudianteDto[]> {
  const res = await clienteEstudiante.get<ApiResponse<BuscarEstudianteDto[]>>(
    `/api/estudiantes/buscar?nombre=${encodeURIComponent(nombre)}`
  );
  return res.data.data;
}

export async function registrarGrupo(datos: RegistrarGrupoRequest): Promise<GrupoDto> {
  const res = await clienteEstudiante.post<ApiResponse<GrupoDto>>('/api/grupos', datos);
  return res.data.data;
}
