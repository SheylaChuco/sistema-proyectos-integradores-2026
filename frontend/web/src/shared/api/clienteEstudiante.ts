import axios from 'axios';
import type { ApiErrorResponse } from '../types/api.types';

const clienteEstudiante = axios.create({
  baseURL: import.meta.env.VITE_API_ESTUDIANTE_URL,
  headers: { 'Content-Type': 'application/json' },
});

clienteEstudiante.interceptors.request.use((config) => {
  const token = localStorage.getItem('token_estudiante');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

clienteEstudiante.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token_estudiante');
      window.location.href = '/login';
    }
    const errorData: ApiErrorResponse = error.response?.data ?? {
      success: false,
      error: { code: 'ERROR_RED', message: 'Error de conexión con el servidor.' },
    };
    return Promise.reject(errorData);
  }
);

export default clienteEstudiante;
