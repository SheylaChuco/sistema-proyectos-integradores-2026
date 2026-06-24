export type EstadoPropuesta = 'PENDIENTE' | 'APROBADO' | 'OBSERVADO' | 'NO_APROBADO';

export interface PropuestaDto {
  id: number;
  grupo_id: number;
  codigo_grupo: string;
  nombre: string;
  descripcion: string;
  estado: EstadoPropuesta;
  comentario_observacion?: string;
  fecha_envio: string;
}

export interface CrearPropuestaRequest {
  nombre: string;
  descripcion: string;
}

export interface EditarPropuestaRequest {
  nombre: string;
  descripcion: string;
}

// Admin
export interface PropuestaAdminDto {
  id: number;
  grupo_codigo: string;
  grupo_periodo: string;
  nombre: string;
  descripcion: string;
  estado: EstadoPropuesta;
  comentario_observacion?: string;
  fecha_envio: string;
  integrantes?: IntegranteAdminDto[];
}

export interface IntegranteAdminDto {
  nombre: string;
  codigo_estudiante: string;
}

export interface ObservarRequest {
  comentario: string;
}
