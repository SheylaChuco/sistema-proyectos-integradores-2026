import axios from 'axios';
import type { ApiErrorResponse } from '../types/api.types';

const clienteAdmin = axios.create({
  baseURL: import.meta.env.VITE_API_ADMIN_URL,
  headers: { 'Content-Type': 'application/json' },
});

clienteAdmin.interceptors.request.use((config) => {
  const token = localStorage.getItem('token_admin');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

clienteAdmin.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token_admin');
      window.location.href = '/admin/login';
    }
    const errorData: ApiErrorResponse = error.response?.data ?? {
      success: false,
      error: { code: 'ERROR_RED', message: 'Error de conexión con el servidor.' },
    };
    return Promise.reject(errorData);
  }
);

export default clienteAdmin;
