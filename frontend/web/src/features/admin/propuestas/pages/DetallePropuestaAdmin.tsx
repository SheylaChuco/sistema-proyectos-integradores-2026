import { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { obtenerPropuesta, aprobarPropuesta, observarPropuesta } from '../services/propuestaAdminService';
import type { PropuestaAdminDto, ObservarRequest } from '../../../../shared/types/propuesta.types';
import type { ApiErrorResponse } from '../../../../shared/types/api.types';

const badgeEstado: Record<string, string> = {
  PENDIENTE:   'bg-yellow-100 text-yellow-700',
  APROBADO:    'bg-green-100 text-green-700',
  OBSERVADO:   'bg-orange-100 text-orange-700',
  NO_APROBADO: 'bg-red-100 text-red-600',
};

const labelEstado: Record<string, string> = {
  PENDIENTE: 'Pendiente', APROBADO: 'Aprobado',
  OBSERVADO: 'Observado', NO_APROBADO: 'No aprobado',
};

export default function DetallePropuestaAdmin() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [propuesta, setPropuesta] = useState<PropuestaAdminDto | null>(null);
  const [cargando, setCargando] = useState(true);
  const [procesando, setProcesando] = useState(false);
  const [modalObservar, setModalObservar] = useState(false);
  const [errorApi, setErrorApi] = useState('');

  const { register, handleSubmit, formState: { errors }, reset } = useForm<ObservarRequest>();

  useEffect(() => {
    obtenerPropuesta(Number(id))
      .then(setPropuesta)
      .catch(() => setErrorApi('No se pudo cargar la propuesta.'))
      .finally(() => setCargando(false));
  }, [id]);

  async function handleAprobar() {
    if (!propuesta) return;
    setProcesando(true);
    try {
      await aprobarPropuesta(propuesta.id);
      navigate('/admin/propuestas', { replace: true });
    } catch (e) {
      const err = e as ApiErrorResponse;
      setErrorApi(err.error?.message ?? 'Error al aprobar.');
      setProcesando(false);
    }
  }

  async function handleObservar(datos: ObservarRequest) {
    if (!propuesta) return;
    setProcesando(true);
    try {
      await observarPropuesta(propuesta.id, datos);
      navigate('/admin/propuestas', { replace: true });
    } catch (e) {
      const err = e as ApiErrorResponse;
      setErrorApi(err.error?.message ?? 'Error al observar.');
      setProcesando(false);
    }
  }

  if (cargando) {
    return (
      <div className="p-8 max-w-3xl">
        <div className="h-4 bg-[#E2E8F0] rounded w-36 mb-6 animate-pulse" />
        <div className="bg-white rounded-xl border border-[#E2E8F0] p-6 animate-pulse">
          <div className="h-6 bg-[#E2E8F0] rounded w-2/3 mb-4" />
          <div className="h-4 bg-[#E2E8F0] rounded w-full mb-2" />
        </div>
      </div>
    );
  }

  return (
    <div className="p-8 max-w-3xl">
      <Link to="/admin/propuestas" className="text-sm text-[#64748B] hover:text-[#0F172A] flex items-center gap-1 mb-6 w-fit">
        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
        </svg>
        Volver a propuestas
      </Link>

      {!propuesta ? (
        <p className="text-[#64748B]">{errorApi || 'No se encontró la propuesta.'}</p>
      ) : (
        <>
          {/* Card propuesta */}
          <div className="bg-white rounded-xl border border-[#E2E8F0] p-6 mb-4">
            <div className="flex flex-wrap items-center gap-2 mb-4">
              <span className="px-2.5 py-0.5 rounded text-xs font-bold bg-[#EEF2F7] text-[#64748B]">{propuesta.grupo_codigo}</span>
              <span className="px-2.5 py-0.5 rounded text-xs font-medium bg-[#EEF2F7] text-[#64748B]">{propuesta.grupo_periodo}</span>
              <span className={`px-2.5 py-0.5 rounded-full text-xs font-semibold ${badgeEstado[propuesta.estado]}`}>
                {labelEstado[propuesta.estado]}
              </span>
            </div>

            <h1 className="text-xl font-bold text-[#0F172A] mb-1">{propuesta.nombre}</h1>
            <p className="text-xs text-[#64748B] mb-4">
              Enviada por {propuesta.integrantes?.[0]?.nombre ?? '—'} · {new Date(propuesta.fecha_envio).toLocaleDateString('es-PE', { day: 'numeric', month: 'short' })}
            </p>
            <p className="text-sm text-[#374151] leading-relaxed">{propuesta.descripcion}</p>

            {errorApi && (
              <div className="mt-4 p-3 rounded-lg bg-red-50 border border-red-200 text-sm text-red-600">{errorApi}</div>
            )}
          </div>

          {/* Integrantes */}
          <div className="bg-white rounded-xl border border-[#E2E8F0] p-6 mb-4">
            <p className="text-xs font-semibold text-[#94A3B8] uppercase tracking-wider mb-4">Integrantes del grupo</p>
            <div className="space-y-px divide-y divide-[#E2E8F0]">
              {(propuesta.integrantes ?? []).map((i, idx) => (
                <div key={idx} className="flex items-center gap-3 py-3 first:pt-0 last:pb-0">
                  <div className="w-8 h-8 rounded-full bg-[#EEF2F7] flex items-center justify-center text-[#2563EB] text-sm font-bold flex-shrink-0">
                    {i.nombre.charAt(0)}
                  </div>
                  <p className="text-sm font-medium text-[#0F172A]">{i.nombre}</p>
                </div>
              ))}
            </div>
          </div>

          {/* Observación registrada — si ya fue observado */}
          {propuesta.estado === 'OBSERVADO' && propuesta.comentario_observacion && (
            <div className="bg-red-50 border border-red-200 rounded-xl p-5 mb-4">
              <p className="text-xs font-semibold text-red-600 uppercase tracking-wider mb-2">Observación registrada</p>
              <p className="text-sm text-[#374151]">{propuesta.comentario_observacion}</p>
            </div>
          )}

          {/* Acciones */}
          {propuesta.estado === 'PENDIENTE' && (
            <div className="flex gap-3">
              <button
                onClick={handleAprobar}
                disabled={procesando}
                className="flex-1 py-3 bg-green-600 hover:bg-green-700 text-white font-semibold text-sm rounded-xl transition disabled:opacity-60 flex items-center justify-center gap-2"
              >
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                </svg>
                {procesando ? 'Procesando...' : 'Aprobar propuesta'}
              </button>
              <button
                onClick={() => { setModalObservar(true); reset(); }}
                disabled={procesando}
                className="flex-1 py-3 border-2 border-red-300 text-red-600 hover:bg-red-50 font-semibold text-sm rounded-xl transition disabled:opacity-60 flex items-center justify-center gap-2"
              >
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v2m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                Observar propuesta
              </button>
            </div>
          )}

          {propuesta.estado === 'OBSERVADO' && (
            <div className="flex gap-3">
              <button
                onClick={handleAprobar}
                disabled={procesando}
                className="flex-1 py-3 bg-green-600 hover:bg-green-700 text-white font-semibold text-sm rounded-xl transition disabled:opacity-60 flex items-center justify-center gap-2"
              >
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                </svg>
                {procesando ? 'Procesando...' : 'Aprobar en segunda revisión'}
              </button>
              <button
                onClick={() => { setModalObservar(true); reset(); }}
                disabled={procesando}
                className="flex-1 py-3 border border-[#CBD5E1] text-[#64748B] hover:bg-[#F8FAFC] font-semibold text-sm rounded-xl transition disabled:opacity-60"
              >
                Nueva observación
              </button>
            </div>
          )}
        </>
      )}

      {/* Modal observar */}
      {modalObservar && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl w-full max-w-md p-6 shadow-xl">
            <div className="flex items-start justify-between mb-4">
              <div>
                <h2 className="text-lg font-bold text-[#0F172A]">Observar propuesta</h2>
                <p className="text-sm text-[#64748B] mt-0.5">El comentario es obligatorio y será visible para el grupo.</p>
              </div>
              <button onClick={() => setModalObservar(false)} className="text-[#94A3B8] hover:text-[#374151] transition">
                <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            <form onSubmit={handleSubmit(handleObservar)} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-[#374151] mb-1">
                  Comentario de observación <span className="text-red-500">*</span>
                </label>
                <textarea
                  rows={4}
                  placeholder="Describe detalladamente qué debe corregir el grupo antes de la próxima revisión..."
                  className="w-full px-3 py-2.5 rounded-lg border border-red-300 text-sm placeholder-[#94A3B8] focus:outline-none focus:ring-2 focus:ring-red-400 focus:border-transparent resize-none"
                  {...register('comentario', { required: 'El comentario es obligatorio.' })}
                />
                {errors.comentario && <p className="mt-1 text-xs text-red-500">{errors.comentario.message}</p>}
              </div>

              <div className="flex gap-3">
                <button
                  type="button"
                  onClick={() => setModalObservar(false)}
                  className="flex-1 py-2.5 border border-[#CBD5E1] text-[#374151] text-sm font-medium rounded-lg hover:bg-[#F8FAFC] transition"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={procesando}
                  className="flex-1 py-2.5 bg-[#2563EB] hover:bg-[#1D4ED8] text-white text-sm font-semibold rounded-lg transition disabled:opacity-60"
                >
                  {procesando ? 'Enviando...' : 'Enviar observación'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
