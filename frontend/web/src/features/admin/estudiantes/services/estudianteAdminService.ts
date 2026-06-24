import clienteAdmin from '../../../../shared/api/clienteAdmin';

export interface EstudianteAdminDto {
  id: number;
  codigo_estudiante: string;
  nombre: string;
  correo: string;
  en_grupo: boolean;
  grupo_codigo: string | null;
  grupo_periodo: string | null;
}

export async function listarEstudiantes(): Promise<EstudianteAdminDto[]> {
  const { data } = await clienteAdmin.get('/api/admin/estudiantes/');
  return data.data;
}
