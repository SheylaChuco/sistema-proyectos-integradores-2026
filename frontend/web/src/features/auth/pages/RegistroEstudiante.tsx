import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import { useAuth } from '../../../context/AuthContext';
import { registrarEstudiante } from '../services/authService';
import type { RegistroRequest } from '../../../shared/types/auth.types';
import type { ApiErrorResponse } from '../../../shared/types/api.types';

export default function RegistroEstudiante() {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<RegistroRequest>();
  const [errorApi, setErrorApi] = useState('');
  const { loginEstudiante: guardarSesion } = useAuth();
  const navigate = useNavigate();

  async function onSubmit(datos: RegistroRequest) {
    setErrorApi('');
    try {
      const resp = await registrarEstudiante(datos);
      guardarSesion(resp.access_token, resp.usuario);
      navigate('/inicio', { replace: true });
    } catch (e) {
      const err = e as ApiErrorResponse;
      setErrorApi(err.error?.message ?? 'Error al crear la cuenta.');
    }
  }

  return (
    <div className="min-h-screen bg-[#EEF2F7] flex items-center justify-center p-4">
      <div className="w-full max-w-[400px]">
        <div className="bg-white rounded-2xl shadow-md border border-[#E2E8F0] p-8">
          {/* Header */}
          <div className="flex items-center gap-3 mb-6">
            <Link to="/login" className="text-[#64748B] hover:text-[#0F172A] transition">
              <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
              </svg>
            </Link>
            <div>
              <h1 className="text-xl font-bold text-[#0F172A]">Crear cuenta</h1>
              <p className="text-sm text-[#2563EB]">Regístrate con tus datos de TECSUP</p>
            </div>
          </div>

          {errorApi && (
            <div className="mb-4 p-3 rounded-lg bg-red-50 border border-red-200 text-sm text-red-600">
              {errorApi}
            </div>
          )}

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-[#2563EB] mb-1">Nombre completo</label>
              <input
                type="text"
                placeholder="Ej: Andrea Torres Quispe"
                className="w-full px-3 py-2.5 rounded-lg border border-[#CBD5E1] text-sm text-[#0F172A] placeholder-[#94A3B8] focus:outline-none focus:ring-2 focus:ring-[#2563EB] focus:border-transparent transition"
                {...register('nombre', { required: 'El nombre es requerido.' })}
              />
              {errors.nombre && <p className="mt-1 text-xs text-red-500">{errors.nombre.message}</p>}
            </div>

            <div>
              <label className="block text-sm font-medium text-[#2563EB] mb-1">Código de estudiante</label>
              <input
                type="text"
                placeholder="Ej: 2023121456"
                className="w-full px-3 py-2.5 rounded-lg border border-[#CBD5E1] text-sm text-[#0F172A] placeholder-[#94A3B8] focus:outline-none focus:ring-2 focus:ring-[#2563EB] focus:border-transparent transition"
                {...register('codigo_estudiante', { required: 'El código es requerido.' })}
              />
              {errors.codigo_estudiante && <p className="mt-1 text-xs text-red-500">{errors.codigo_estudiante.message}</p>}
            </div>

            <div>
              <label className="block text-sm font-medium text-[#2563EB] mb-1">Correo institucional</label>
              <input
                type="email"
                placeholder="nombre@tecsup.edu.pe"
                className="w-full px-3 py-2.5 rounded-lg border border-[#CBD5E1] text-sm text-[#0F172A] placeholder-[#94A3B8] focus:outline-none focus:ring-2 focus:ring-[#2563EB] focus:border-transparent transition"
                {...register('correo', {
                  required: 'El correo es requerido.',
                  pattern: { value: /^[^@]+@tecsup\.edu\.pe$/, message: 'Usa tu correo @tecsup.edu.pe.' },
                })}
              />
              {errors.correo && <p className="mt-1 text-xs text-red-500">{errors.correo.message}</p>}
            </div>

            <div>
              <label className="block text-sm font-medium text-[#2563EB] mb-1">Contraseña</label>
              <input
                type="password"
                placeholder="Mínimo 8 caracteres"
                className="w-full px-3 py-2.5 rounded-lg border border-[#CBD5E1] text-sm text-[#0F172A] placeholder-[#94A3B8] focus:outline-none focus:ring-2 focus:ring-[#2563EB] focus:border-transparent transition"
                {...register('password', {
                  required: 'La contraseña es requerida.',
                  minLength: { value: 8, message: 'Mínimo 8 caracteres.' },
                })}
              />
              {errors.password && <p className="mt-1 text-xs text-red-500">{errors.password.message}</p>}
            </div>

            <button
              type="submit"
              disabled={isSubmitting}
              className="w-full py-3 bg-[#2563EB] hover:bg-[#1D4ED8] text-white font-semibold rounded-lg text-sm transition disabled:opacity-60 disabled:cursor-not-allowed mt-2"
            >
              {isSubmitting ? 'Creando cuenta...' : 'Crear cuenta'}
            </button>
          </form>

          <p className="mt-5 text-center text-sm text-[#64748B]">
            ¿Ya tienes cuenta?{' '}
            <Link to="/login" className="text-[#2563EB] font-semibold hover:underline">
              Inicia sesión
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
