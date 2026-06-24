import clienteEstudiante from '../../../shared/api/clienteEstudiante';
import type { ApiResponse } from '../../../shared/types/api.types';
import type { PropuestaDto, CrearPropuestaRequest, EditarPropuestaRequest } from '../../../shared/types/propuesta.types';
import type { ProyectoDetalle } from '../../../shared/types/proyecto.types';

export async function obtenerMiPropuesta(): Promise<PropuestaDto | null> {
  try {
    const res = await clienteEstudiante.get<ApiResponse<PropuestaDto>>('/api/propuestas/mi-propuesta');
    return res.data.data ?? null;
  } catch (e) {
    const err = e as { error?: { code?: string } };
    if (err?.error?.code === 'NO_ENCONTRADO') return null;
    throw e; // propaga SIN_GRUPO y otros errores al componente
  }
}

export async function crearPropuesta(datos: CrearPropuestaRequest): Promise<PropuestaDto> {
  const res = await clienteEstudiante.post<ApiResponse<PropuestaDto>>('/api/propuestas', datos);
  return res.data.data;
}

export async function editarPropuesta(id: number, datos: EditarPropuestaRequest): Promise<PropuestaDto> {
  const res = await clienteEstudiante.put<ApiResponse<PropuestaDto>>(`/api/propuestas/${id}`, datos);
  return res.data.data;
}

export async function obtenerMiProyecto(): Promise<ProyectoDetalle | null> {
  try {
    const res = await clienteEstudiante.get<ApiResponse<ProyectoDetalle>>('/api/proyectos/mi-proyecto');
    return res.data.data;
  } catch {
    return null;
  }
}

export async function actualizarUrlProyecto(proyectoId: number, url: string): Promise<ProyectoDetalle> {
  const res = await clienteEstudiante.put<ApiResponse<ProyectoDetalle>>(`/api/proyectos/${proyectoId}/url`, { url });
  return res.data.data;
}
