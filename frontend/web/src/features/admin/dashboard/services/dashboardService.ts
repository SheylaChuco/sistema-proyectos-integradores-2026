import clienteAdmin from '../../../../shared/api/clienteAdmin';
import type { ApiResponse } from '../../../../shared/types/api.types';

export interface EstadisticasDto {
  total_proyectos: number;
  proyectos_historicos: number;
  proyectos_nuevos: number;
  propuestas_pendientes: number;
  propuestas_aprobadas: number;
  propuestas_observadas: number;
  proyectos_en_desarrollo: number;
  proyectos_aprobados: number;
  proyectos_no_aprobados: number;
  por_ciclo: { ciclo: string; total: number }[];
}

export async function obtenerEstadisticas(): Promise<EstadisticasDto> {
  const res = await clienteAdmin.get<ApiResponse<EstadisticasDto>>('/api/admin/estadisticas');
  return res.data.data;
}
