import clienteEstudiante from '../../../shared/api/clienteEstudiante';
import type { ApiResponse } from '../../../shared/types/api.types';

export interface MensajeChat {
  role: 'user' | 'assistant';
  content: string;
}

export interface ChatRequest {
  mensaje: string;
  historial: MensajeChat[];
}

export interface ChatResponse {
  respuesta: string;
}

export async function enviarMensaje(datos: ChatRequest): Promise<string> {
  const res = await clienteEstudiante.post<ApiResponse<ChatResponse>>('/api/asistente/chat', datos);
  return res.data.data.respuesta;
}
