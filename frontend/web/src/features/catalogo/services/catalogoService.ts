import clienteEstudiante from '../../../shared/api/clienteEstudiante';
import type { ApiResponse, PaginaDto } from '../../../shared/types/api.types';
import type { ProyectoLista, ProyectoDetalle } from '../../../shared/types/proyecto.types';

export interface FiltrosCatalogo {
  ciclo?: string;
  origen?: 'HISTORICO' | 'NUEVO';
  busqueda?: string;
  page?: number;
  size?: number;
}

export async function listarProyectos(filtros: FiltrosCatalogo = {}): Promise<PaginaDto<ProyectoLista>> {
  const params = new URLSearchParams();
  if (filtros.ciclo) params.set('ciclo', filtros.ciclo);
  if (filtros.origen) params.set('origen', filtros.origen);
  if (filtros.busqueda) params.set('busqueda', filtros.busqueda);
  params.set('page', String(filtros.page ?? 0));
  params.set('size', String(filtros.size ?? 12));
  const res = await clienteEstudiante.get<ApiResponse<PaginaDto<ProyectoLista>>>(`/api/proyectos?${params}`);
  return res.data.data;
}

export async function obtenerProyecto(id: number): Promise<ProyectoDetalle> {
  const res = await clienteEstudiante.get<ApiResponse<ProyectoDetalle>>(`/api/proyectos/${id}`);
  return res.data.data;
}

export async function obtenerCiclos(): Promise<string[]> {
  const res = await clienteEstudiante.get<ApiResponse<string[]>>('/api/proyectos/ciclos');
  return res.data.data;
}
