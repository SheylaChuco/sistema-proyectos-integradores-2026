import { useEffect, useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import {
  obtenerMiPropuesta,
  crearPropuesta,
  editarPropuesta,
  obtenerMiProyecto,
  actualizarUrlProyecto,
} from '../services/propuestaService';
import type { PropuestaDto } from '../../../shared/types/propuesta.types';
import type { ProyectoDetalle } from '../../../shared/types/proyecto.types';
import type { ApiErrorResponse } from '../../../shared/types/api.types';

type FormData = { nombre: string; descripcion: string };

export default function MiPropuesta() {
  const [propuesta, setPropuesta] = useState<PropuestaDto | null>(null);
  const [proyecto, setProyecto] = useState<ProyectoDetalle | null>(null);
  const [cargando, setCargando] = useState(true);
  const [sinGrupo, setSinGrupo] = useState(false);
  const [editando, setEditando] = useState(false);
  const [errorApi, setErrorApi] = useState('');
  const [urlInput, setUrlInput] = useState('');
  const [guardandoUrl, setGuardandoUrl] = useState(false);
  const [urlGuardada, setUrlGuardada] = useState(false);

  const { register, handleSubmit, formState: { errors, isSubmitting }, reset } = useForm<FormData>();

  const cargar = useCallback(async () => {
    setCargando(true);
    try {
      const data = await obtenerMiPropuesta();
      setPropuesta(data);
      if (data?.estado === 'APROBADO') {
        const proy = await obtenerMiProyecto();
        setProyecto(proy);
        setUrlInput(proy?.url ?? '');
      }
    } catch (e) {
      const err = e as ApiErrorResponse;
      if (err?.error?.code === 'SIN_GRUPO') setSinGrupo(true);
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => { cargar(); }, [cargar]);

  async function onSubmitCrear(datos: FormData) {
    setErrorApi('');
    try {
      setPropuesta(await crearPropuesta(datos));
      reset();
    } catch (e) {
      const err = e as ApiErrorResponse;
      setErrorApi(err.error?.message ?? 'Error al enviar la propuesta.');
    }
  }

  async function onGuardarUrl() {
    if (!proyecto) return;
    setGuardandoUrl(true);
    try {
      const actualizado = await actualizarUrlProyecto(proyecto.id, urlInput.trim());
      setProyecto(actualizado);
      setUrlGuardada(true);
      setTimeout(() => setUrlGuardada(false), 3000);
    } catch (e) {
      const err = e as ApiErrorResponse;
      setErrorApi(err.error?.message ?? 'Error al guardar la URL.');
    } finally {
      setGuardandoUrl(false);
    }
  }

  async function onSubmitEditar(datos: FormData) {
    if (!propuesta) return;
    setErrorApi('');
    try {
      setPropuesta(await editarPropuesta(propuesta.id, datos));
      setEditando(false);
      reset();
    } catch (e) {
      const err = e as ApiErrorResponse;
      setErrorApi(err.error?.message ?? 'Error al enviar la corrección.');
    }
  }

  if (cargando) {
    return (
      <div className="p-8">
        <div className="h-7 bg-[#E2E8F0] rounded w-40 mb-6 animate-pulse" />
        <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm p-6 animate-pulse">
          <div className="h-5 bg-[#E2E8F0] rounded w-1/3 mb-4" />
          <div className="h-4 bg-[#E2E8F0] rounded w-full" />
        </div>
      </div>
    );
  }

  return (
    <div className="p-8 max-w-2xl">
      <h1 className="text-2xl font-bold text-[#0F172A] mb-6">Mi Propuesta</h1>

      {/* ── Sin grupo ── */}
      {sinGrupo && (
        <div className="bg-white rounded-xl border border-[#E2E8F0] p-10 text-center">
          <div className="w-14 h-14 rounded-2xl bg-yellow-50 flex items-center justify-center mx-auto mb-4">
            <svg className="w-7 h-7 text-yellow-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
          </div>
          <h2 className="text-base font-semibold text-[#0F172A]">Necesitas un grupo primero</h2>
          <p className="text-sm text-[#64748B] mt-2 max-w-xs mx-auto">
            Para registrar una propuesta de proyecto integrador, primero debes pertenecer a un grupo activo en el ciclo actual.
          </p>
          <Link
            to="/mi-grupo"
            className="mt-5 inline-block px-6 py-2.5 bg-[#2563EB] hover:bg-[#1D4ED8] text-white font-semibold text-sm rounded-lg transition"
          >
            Registrar mi grupo
          </Link>
        </div>
      )}

      {/* ── Con grupo, sin propuesta ── */}
      {!sinGrupo && !propuesta && !cargando && (
        <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm p-6">
          <h2 className="text-base font-semibold text-[#0F172A]">Nueva propuesta de proyecto</h2>
          <p className="text-sm text-[#2563EB] mt-0.5 mb-5">Completa los datos de tu proyecto integrador</p>

          {errorApi && (
            <div className="mb-4 p-3 rounded-lg bg-red-50 border border-red-200 text-sm text-red-600">{errorApi}</div>
          )}

          <form onSubmit={handleSubmit(onSubmitCrear)} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-[#2563EB] mb-1">Título del proyecto</label>
              <input
                type="text"
                placeholder="Nombre descriptivo y conciso"
                className="w-full px-3 py-2.5 rounded-lg border border-[#CBD5E1] text-sm placeholder-[#94A3B8] focus:outline-none focus:ring-2 focus:ring-[#2563EB] focus:border-transparent"
                {...register('nombre', { required: 'El título es requerido.', maxLength: { value: 255, message: 'Máximo 255 caracteres.' } })}
              />
              {errors.nombre && <p className="mt-1 text-xs text-red-500">{errors.nombre.message}</p>}
            </div>

            <div>
              <label className="block text-sm font-medium text-[#2563EB] mb-1">Descripción del proyecto</label>
              <textarea
                rows={5}
                placeholder="Describe el objetivo, alcance y tecnologías a utilizar..."
                className="w-full px-3 py-2.5 rounded-lg border border-[#CBD5E1] text-sm placeholder-[#94A3B8] focus:outline-none focus:ring-2 focus:ring-[#2563EB] focus:border-transparent resize-none"
                {...register('descripcion', { required: 'La descripción es requerida.' })}
              />
              {errors.descripcion && <p className="mt-1 text-xs text-red-500">{errors.descripcion.message}</p>}
            </div>

            <button
              type="submit"
              disabled={isSubmitting}
              className="w-full py-3 bg-[#2563EB] hover:bg-[#1D4ED8] text-white font-semibold text-sm rounded-lg transition disabled:opacity-60"
            >
              {isSubmitting ? 'Enviando...' : 'Enviar propuesta'}
            </button>
          </form>
        </div>
      )}

      {/* ── Propuesta existente ── */}
      {propuesta && !editando && (
        <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm p-6">
          {/* Banner estado */}
          {propuesta.estado === 'PENDIENTE' && (
            <div className="mb-5 p-3.5 rounded-lg bg-yellow-50 border border-yellow-200">
              <div className="flex items-center gap-2">
                <div className="w-2 h-2 rounded-full bg-yellow-500" />
                <p className="text-sm font-semibold text-yellow-700">Propuesta en revisión</p>
              </div>
              <p className="text-xs text-yellow-600 mt-0.5 ml-4">
                Enviada el {new Date(propuesta.fecha_envio).toLocaleDateString('es-PE', { day: 'numeric', month: 'long', year: 'numeric' })} · En espera de revisión del encargado
              </p>
            </div>
          )}

          {propuesta.estado === 'OBSERVADO' && (
            <div className="mb-5 p-3.5 rounded-lg bg-red-50 border border-red-200">
              <div className="flex items-center gap-2">
                <svg className="w-4 h-4 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
                <p className="text-sm font-semibold text-red-700">Tu propuesta fue observada</p>
              </div>
              {propuesta.comentario_observacion && (
                <p className="text-xs text-red-600 mt-1 ml-6">{propuesta.comentario_observacion}</p>
              )}
            </div>
          )}

          {propuesta.estado === 'APROBADO' && (
            <>
              {/* EN_DESARROLLO — propuesta aprobada, proyecto en curso */}
              {(!proyecto || proyecto.estado === 'EN_DESARROLLO') && (
                <div className="mb-5 p-3.5 rounded-lg bg-green-50 border border-green-200">
                  <div className="flex items-center gap-2">
                    <svg className="w-4 h-4 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                      <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                    </svg>
                    <p className="text-sm font-semibold text-green-700">Propuesta aprobada · Proyecto en desarrollo</p>
                  </div>
                </div>
              )}

              {/* SUSTENTACIÓN APROBADA */}
              {proyecto?.estado === 'APROBADO' && (
                <div className="mb-5 p-4 rounded-lg bg-green-50 border border-green-300">
                  <div className="flex items-center gap-2 mb-1">
                    <div className="w-7 h-7 rounded-full bg-green-500 flex items-center justify-center flex-shrink-0">
                      <svg className="w-4 h-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                        <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                      </svg>
                    </div>
                    <p className="text-base font-bold text-green-700">¡Sustentación aprobada!</p>
                  </div>
                  <p className="text-xs text-green-600 ml-9">Tu proyecto superó la evaluación final exitosamente.</p>
                  {proyecto.comentario_evaluacion && (
                    <div className="mt-3 ml-9 p-3 bg-white rounded-lg border border-green-200">
                      <p className="text-xs font-semibold text-[#64748B] mb-1">Comentario del evaluador</p>
                      <p className="text-sm text-[#374151]">{proyecto.comentario_evaluacion}</p>
                    </div>
                  )}
                </div>
              )}

              {/* SUSTENTACIÓN NO APROBADA */}
              {proyecto?.estado === 'NO_APROBADO' && (
                <div className="mb-5 p-4 rounded-lg bg-red-50 border border-red-300">
                  <div className="flex items-center gap-2 mb-1">
                    <div className="w-7 h-7 rounded-full bg-red-500 flex items-center justify-center flex-shrink-0">
                      <svg className="w-4 h-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                        <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                      </svg>
                    </div>
                    <p className="text-base font-bold text-red-700">Sustentación no aprobada</p>
                  </div>
                  <p className="text-xs text-red-600 ml-9">El ciclo ha cerrado para tu grupo en este periodo.</p>
                  {proyecto.comentario_evaluacion && (
                    <div className="mt-3 ml-9 p-3 bg-white rounded-lg border border-red-200">
                      <p className="text-xs font-semibold text-[#64748B] mb-1">Comentario del evaluador</p>
                      <p className="text-sm text-[#374151]">{proyecto.comentario_evaluacion}</p>
                    </div>
                  )}
                </div>
              )}

              {/* URL del repositorio — solo si el proyecto está EN_DESARROLLO */}
              {proyecto && proyecto.estado === 'EN_DESARROLLO' && (
                <div className="mb-5 p-4 rounded-lg bg-[#F8FAFC] border border-[#E2E8F0]">
                  <p className="text-sm font-semibold text-[#0F172A] mb-1">URL del repositorio</p>
                  <p className="text-xs text-[#64748B] mb-3">
                    Registra el enlace de tu repositorio (opcional). Puedes actualizarlo mientras el proyecto esté en desarrollo.
                  </p>
                  {errorApi && (
                    <div className="mb-3 p-2.5 rounded-lg bg-red-50 border border-red-200 text-xs text-red-600">{errorApi}</div>
                  )}
                  {urlGuardada && (
                    <div className="mb-3 p-2.5 rounded-lg bg-green-50 border border-green-200 text-xs text-green-700">URL guardada correctamente.</div>
                  )}
                  <div className="flex gap-2">
                    <input
                      type="url"
                      value={urlInput}
                      onChange={(e) => setUrlInput(e.target.value)}
                      placeholder="https://github.com/tu-usuario/tu-repo"
                      className="flex-1 px-3 py-2 rounded-lg border border-[#CBD5E1] text-sm placeholder-[#94A3B8] focus:outline-none focus:ring-2 focus:ring-[#2563EB] focus:border-transparent"
                    />
                    <button
                      onClick={onGuardarUrl}
                      disabled={guardandoUrl || !urlInput.trim()}
                      className="px-4 py-2 bg-[#2563EB] hover:bg-[#1D4ED8] text-white text-sm font-semibold rounded-lg transition disabled:opacity-50"
                    >
                      {guardandoUrl ? 'Guardando...' : 'Guardar'}
                    </button>
                  </div>
                </div>
              )}
            </>
          )}

          {/* Tags */}
          <div className="flex flex-wrap gap-2 mb-3">
            {propuesta.codigo_grupo && (
              <span className="px-2.5 py-0.5 rounded-full text-xs font-medium bg-[#EEF2F7] text-[#64748B]">
                Grupo {propuesta.codigo_grupo}
              </span>
            )}
          </div>

          <h2 className="text-lg font-bold text-[#0F172A] mb-2">{propuesta.nombre}</h2>
          <p className="text-sm text-[#374151] leading-relaxed">{propuesta.descripcion}</p>

          {propuesta.estado === 'OBSERVADO' && (
            <button
              onClick={() => {
                reset({ nombre: propuesta.nombre, descripcion: propuesta.descripcion });
                setEditando(true);
                setErrorApi('');
              }}
              className="mt-5 w-full py-3 bg-[#2563EB] hover:bg-[#1D4ED8] text-white font-semibold text-sm rounded-lg transition"
            >
              Corregir propuesta
            </button>
          )}
        </div>
      )}

      {/* ── Formulario corrección (OBSERVADO) ── */}
      {propuesta && editando && (
        <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm p-6">
          <h2 className="text-base font-semibold text-[#0F172A]">Corregir propuesta</h2>
          <p className="text-sm text-[#64748B] mt-0.5 mb-5">Edita considerando las observaciones del encargado.</p>

          {errorApi && (
            <div className="mb-4 p-3 rounded-lg bg-red-50 border border-red-200 text-sm text-red-600">{errorApi}</div>
          )}

          <form onSubmit={handleSubmit(onSubmitEditar)} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-[#2563EB] mb-1">Título del proyecto</label>
              <input
                type="text"
                className="w-full px-3 py-2.5 rounded-lg border border-[#CBD5E1] text-sm focus:outline-none focus:ring-2 focus:ring-[#2563EB] focus:border-transparent"
                {...register('nombre', { required: 'El título es requerido.', maxLength: { value: 255, message: 'Máximo 255 caracteres.' } })}
              />
              {errors.nombre && <p className="mt-1 text-xs text-red-500">{errors.nombre.message}</p>}
            </div>

            <div>
              <label className="block text-sm font-medium text-[#2563EB] mb-1">Descripción actualizada</label>
              <textarea
                rows={5}
                className="w-full px-3 py-2.5 rounded-lg border border-[#CBD5E1] text-sm focus:outline-none focus:ring-2 focus:ring-[#2563EB] focus:border-transparent resize-none"
                {...register('descripcion', { required: 'La descripción es requerida.' })}
              />
              {errors.descripcion && <p className="mt-1 text-xs text-red-500">{errors.descripcion.message}</p>}
            </div>

            <div className="flex gap-3">
              <button
                type="submit"
                disabled={isSubmitting}
                className="flex-1 py-3 bg-[#2563EB] hover:bg-[#1D4ED8] text-white font-semibold text-sm rounded-lg transition disabled:opacity-60"
              >
                {isSubmitting ? 'Enviando...' : 'Enviar corrección'}
              </button>
              <button
                type="button"
                onClick={() => { setEditando(false); setErrorApi(''); }}
                className="px-5 py-3 border border-[#CBD5E1] text-sm text-[#64748B] rounded-lg hover:bg-[#F8FAFC] transition"
              >
                Cancelar
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}
