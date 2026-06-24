export type OrigenProyecto = 'HISTORICO' | 'NUEVO';
export type EstadoProyecto = 'EN_DESARROLLO' | 'APROBADO' | 'NO_APROBADO';

export interface IntegranteDto {
  id: number;
  nombre: string;
  codigo_estudiante: string;
}

export interface ProyectoLista {
  id: number;
  origen: OrigenProyecto;
  nombre: string;
  descripcion: string;
  ciclo: string;
  codigo_seccion?: string;
  codigo_grupo?: string;
  estado?: EstadoProyecto;
}

export interface ProyectoDetalle extends ProyectoLista {
  integrantes?: IntegranteDto[];
  url?: string;
  comentario_evaluacion?: string;
}
