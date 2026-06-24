import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import { useAuth } from '../../../context/AuthContext';
import { loginAdmin } from '../services/authService';
import type { LoginRequest } from '../../../shared/types/auth.types';
import type { ApiErrorResponse } from '../../../shared/types/api.types';

export default function LoginAdmin() {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<LoginRequest>();
  const [errorApi, setErrorApi] = useState('');
  const { loginAdmin: guardarSesion } = useAuth();
  const navigate = useNavigate();

  async function onSubmit(datos: LoginRequest) {
    setErrorApi('');
    try {
      const resp = await loginAdmin(datos);
      guardarSesion(resp.access_token, resp.usuario);
      navigate('/admin/propuestas', { replace: true });
    } catch (e) {
      const err = e as ApiErrorResponse;
      setErrorApi(err.error?.message ?? 'Correo o contraseña incorrectos.');
    }
  }

  return (
    <div className="min-h-screen bg-[#EEF2F7] flex items-center justify-center p-4">
      <div className="w-full max-w-[400px]">
        <div className="bg-white rounded-2xl shadow-md border border-[#E2E8F0] p-8">
          {/* Logo */}
          <div className="flex flex-col items-center mb-7">
            <div className="w-12 h-12 rounded-xl bg-[#0B2B5C] flex items-center justify-center mb-3">
              <span className="text-white font-bold text-xl leading-none">+</span>
            </div>
            <h1 className="text-xl font-bold text-[#0F172A]">Integratec</h1>
            <div className="flex items-center gap-1.5 mt-1">
              <svg className="w-3.5 h-3.5 text-[#2563EB]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
              <p className="text-sm text-[#2563EB]">Panel Administrativo</p>
            </div>
          </div>

          {errorApi && (
            <div className="mb-4 p-3 rounded-lg bg-red-50 border border-red-200 text-sm text-red-600">
              {errorApi}
            </div>
          )}

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-[#374151] mb-1">Correo institucional</label>
              <input
                type="email"
                placeholder="encargado@tecsup.edu.pe"
                className="w-full px-3 py-2.5 rounded-lg border border-[#CBD5E1] text-sm text-[#0F172A] placeholder-[#94A3B8] focus:outline-none focus:ring-2 focus:ring-[#0B2B5C] focus:border-transparent transition"
                {...register('correo', { required: 'El correo es requerido.' })}
              />
              {errors.correo && <p className="mt-1 text-xs text-red-500">{errors.correo.message}</p>}
            </div>

            <div>
              <label className="block text-sm font-medium text-[#374151] mb-1">Contraseña</label>
              <input
                type="password"
                placeholder="••••••••"
                className="w-full px-3 py-2.5 rounded-lg border border-[#CBD5E1] text-sm text-[#0F172A] placeholder-[#94A3B8] focus:outline-none focus:ring-2 focus:ring-[#0B2B5C] focus:border-transparent transition"
                {...register('password', { required: 'La contraseña es requerida.' })}
              />
              {errors.password && <p className="mt-1 text-xs text-red-500">{errors.password.message}</p>}
            </div>

            <button
              type="submit"
              disabled={isSubmitting}
              className="w-full py-3 bg-[#0B2B5C] hover:bg-[#0D3570] text-white font-semibold rounded-lg text-sm transition disabled:opacity-60 disabled:cursor-not-allowed mt-2"
            >
              {isSubmitting ? 'Verificando...' : 'Ingresar al panel'}
            </button>
          </form>

          <p className="mt-5 text-center text-sm text-[#64748B]">
            ¿Eres estudiante?{' '}
            <Link to="/login" className="text-[#2563EB] font-semibold hover:underline">
              Acceder como estudiante
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
