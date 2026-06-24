import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import { useAuth } from '../../../context/AuthContext';
import { loginEstudiante } from '../services/authService';
import type { LoginRequest } from '../../../shared/types/auth.types';
import type { ApiErrorResponse } from '../../../shared/types/api.types';

export default function LoginEstudiante() {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<LoginRequest>();
  const [errorApi, setErrorApi] = useState('');
  const { loginEstudiante: guardarSesion } = useAuth();
  const navigate = useNavigate();

  async function onSubmit(datos: LoginRequest) {
    setErrorApi('');
    try {
      const resp = await loginEstudiante(datos);
      guardarSesion(resp.access_token, resp.usuario);
      navigate('/inicio', { replace: true });
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
            <p className="text-sm text-[#64748B] mt-0.5">Proyectos integradores · TECSUP</p>
          </div>

          {errorApi && (
            <div className="mb-4 p-3 rounded-lg bg-red-50 border border-red-200 text-sm text-red-600">
              {errorApi}
            </div>
          )}

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-[#2563EB] mb-1">
                Correo institucional
              </label>
              <input
                type="email"
                placeholder="nombre@tecsup.edu.pe"
                className="w-full px-3 py-2.5 rounded-lg border border-[#CBD5E1] text-sm text-[#0F172A] placeholder-[#94A3B8] focus:outline-none focus:ring-2 focus:ring-[#2563EB] focus:border-transparent transition"
                {...register('correo', {
                  required: 'El correo es requerido.',
                  pattern: { value: /^[^@]+@tecsup\.edu\.pe$/, message: 'Ingresa tu correo @tecsup.edu.pe.' },
                })}
              />
              {errors.correo && <p className="mt-1 text-xs text-red-500">{errors.correo.message}</p>}
            </div>

            <div>
              <label className="block text-sm font-medium text-[#2563EB] mb-1">
                Contraseña
              </label>
              <input
                type="password"
                placeholder="••••••••"
                className="w-full px-3 py-2.5 rounded-lg border border-[#CBD5E1] text-sm text-[#0F172A] placeholder-[#94A3B8] focus:outline-none focus:ring-2 focus:ring-[#2563EB] focus:border-transparent transition"
                {...register('password', { required: 'La contraseña es requerida.' })}
              />
              {errors.password && <p className="mt-1 text-xs text-red-500">{errors.password.message}</p>}
            </div>

            <button
              type="submit"
              disabled={isSubmitting}
              className="w-full py-3 bg-[#2563EB] hover:bg-[#1D4ED8] text-white font-semibold rounded-lg text-sm transition disabled:opacity-60 disabled:cursor-not-allowed mt-2"
            >
              {isSubmitting ? 'Ingresando...' : 'Ingresar a mi cuenta'}
            </button>
          </form>

          <div className="mt-5 flex items-center gap-3">
            <div className="flex-1 h-px bg-[#E2E8F0]" />
            <div className="w-2 h-2 rounded-full bg-[#CBD5E1]" />
            <div className="flex-1 h-px bg-[#E2E8F0]" />
          </div>

          <div className="mt-5 text-center space-y-3">
            <p className="text-sm text-[#64748B]">
              ¿No tienes cuenta?{' '}
              <Link to="/registro" className="text-[#2563EB] font-semibold hover:underline">
                Regístrate aquí
              </Link>
            </p>
            <p className="text-xs text-[#94A3B8]">
              ¿Eres encargado de proyectos?{' '}
              <Link to="/admin/login" className="text-[#2563EB] hover:underline">
                Panel administrativo
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
