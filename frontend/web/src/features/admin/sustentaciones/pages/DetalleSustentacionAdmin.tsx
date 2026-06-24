import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  obtenerProyectoSustentacion,
  evaluarProyecto,
  type ProyectoSustentacionDto,
} from '../services/sustentacionService';

export default function DetalleSustentacionAdmin() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [proyecto, setProyecto] = useState<ProyectoSustentacionDto | null>(null);
  const [cargando, setCargando] = useState(true);
  const [comentario, setComentario] = useState('');
  const [enviando, setEnviando] = useState<'APROBADO' | 'NO_APROBADO' | null>(null);
  const [confirmando, setConfirmando] = useState<'APROBADO' | 'NO_APROBADO' | null>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!id) return;
    obtenerProyectoSustentacion(Number(id))
      .then(setProyecto)
      .catch(() => navigate('/admin/sustentaciones', { replace: true }))
      .finally(() => setCargando(false));
  }, [id, navigate]);

  async function handleEvaluar(estado: 'APROBADO' | 'NO_APROBADO') {
    if (!proyecto) return;
    setEnviando(estado);
    setError('');
    try {
      await evaluarProyecto(proyecto.id, { estado, comentario: comentario.trim() || undefined });
      navigate('/admin/sustentaciones', { replace: true });
    } catch {
      setError('Ocurrió un error al registrar la evaluación. Intenta nuevamente.');
      setEnviando(null);
      setConfirmando(null);
    }
  }

  if (cargando) {
    return (
      <div className="p-8 max-w-2xl">
        <div className="h-6 bg-[#E2E8F0] rounded w-32 mb-6 animate-pulse" />
        <div className="bg-white rounded-xl border border-[#E2E8F0] p-6 space-y-4 animate-pulse">
          <div className="h-5 bg-[#E2E8F0] rounded w-2/3" />
          <div className="h-4 bg-[#E2E8F0] rounded w-full" />
          <div className="h-4 bg-[#E2E8F0] rounded w-4/5" />
        </div>
      </div>
    );
  }

  if (!proyecto) return null;

  return (
    <div className="p-8 max-w-2xl">
      {/* Volver */}
      <button
        onClick={() => navigate('/admin/sustentaciones')}
        className="flex items-center gap-1.5 text-sm text-[#64748B] hover:text-[#0F172A] mb-5 transition"
      >
        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
        </svg>
        Volver a sustentaciones
      </button>

      {/* Cabecera */}
      <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm p-6 mb-4">
        <div className="flex flex-wrap gap-2 mb-3">
          <span className="text-xs font-medium bg-[#EEF2F7] text-[#64748B] px-2.5 py-0.5 rounded-full">
            {proyecto.grupo_codigo}
          </span>
          <span className="text-xs text-[#94A3B8] bg-[#EEF2F7] px-2.5 py-0.5 rounded-full">
            {proyecto.grupo_periodo}
          </span>
          <span className="text-xs font-medium bg-blue-50 text-[#2563EB] px-2.5 py-0.5 rounded-full">
            En desarrollo
          </span>
        </div>

        <h1 className="text-xl font-bold text-[#0F172A] mb-2">{proyecto.nombre}</h1>
        <p className="text-sm text-[#374151] leading-relaxed">{proyecto.descripcion}</p>

        {/* URL repositorio */}
        {proyecto.url && (
          <div className="mt-4 pt-4 border-t border-[#E2E8F0]">
            <p className="text-xs font-semibold text-[#64748B] uppercase tracking-wide mb-1">Repositorio</p>
            <a
              href={proyecto.url}
              target="_blank"
              rel="noopener noreferrer"
              className="text-sm text-[#2563EB] hover:underline break-all"
            >
              {proyecto.url}
            </a>
          </div>
        )}

        {/* Integrantes */}
        {proyecto.integrantes && proyecto.integrantes.length > 0 && (
          <div className="mt-4 pt-4 border-t border-[#E2E8F0]">
            <p className="text-xs font-semibold text-[#64748B] uppercase tracking-wide mb-2">Integrantes</p>
            <div className="space-y-1.5">
              {proyecto.integrantes.map((i, idx) => (
                <div key={idx} className="flex items-center gap-2">
                  <div className="w-6 h-6 rounded-full bg-[#EEF2F7] flex items-center justify-center text-xs font-bold text-[#64748B] flex-shrink-0">
                    {i.nombre.charAt(0)}
                  </div>
                  <span className="text-sm text-[#374151]">{i.nombre}</span>
                  <span className="text-xs text-[#94A3B8]">· {i.codigo_estudiante}</span>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>

      {/* Evaluación */}
      <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm p-6">
        <p className="text-sm font-semibold text-[#0F172A] mb-1">Evaluación de sustentación</p>
        <p className="text-xs text-[#64748B] mb-4">
          El comentario es opcional. La decisión es definitiva y no puede revertirse.
        </p>

        {error && (
          <div className="mb-4 p-3 rounded-lg bg-red-50 border border-red-200 text-sm text-red-600">{error}</div>
        )}

        <div className="mb-4">
          <label className="block text-sm font-medium text-[#374151] mb-1.5">
            Comentario de evaluación <span className="text-[#94A3B8] font-normal">(opcional)</span>
          </label>
          <textarea
            rows={3}
            value={comentario}
            onChange={(e) => setComentario(e.target.value)}
            placeholder="Observaciones sobre la sustentación del proyecto..."
            className="w-full px-3 py-2.5 rounded-lg border border-[#CBD5E1] text-sm placeholder-[#94A3B8] focus:outline-none focus:ring-2 focus:ring-[#2563EB] focus:border-transparent resize-none"
          />
        </div>

        {/* Advertencia antes de confirmar */}
        {confirmando && (
          <div className={`mb-4 p-4 rounded-lg border ${
            confirmando === 'APROBADO'
              ? 'bg-green-50 border-green-200'
              : 'bg-red-50 border-red-200'
          }`}>
            <p className={`text-sm font-semibold mb-1 ${confirmando === 'APROBADO' ? 'text-green-700' : 'text-red-700'}`}>
              ¿Confirmar decisión?
            </p>
            <p className="text-xs text-[#64748B] mb-3">
              Vas a marcar este proyecto como <strong>{confirmando === 'APROBADO' ? 'Aprobado' : 'No aprobado'}</strong>.
              Esta acción es definitiva y no puede revertirse.
            </p>
            <div className="flex gap-2">
              <button
                onClick={() => handleEvaluar(confirmando)}
                disabled={!!enviando}
                className={`px-4 py-2 text-sm font-semibold text-white rounded-lg transition disabled:opacity-60 ${
                  confirmando === 'APROBADO'
                    ? 'bg-green-600 hover:bg-green-700'
                    : 'bg-red-600 hover:bg-red-700'
                }`}
              >
                {enviando ? 'Guardando...' : 'Sí, confirmar'}
              </button>
              <button
                onClick={() => setConfirmando(null)}
                disabled={!!enviando}
                className="px-4 py-2 text-sm text-[#64748B] border border-[#CBD5E1] rounded-lg hover:bg-[#F8FAFC] transition"
              >
                Cancelar
              </button>
            </div>
          </div>
        )}

        {!confirmando && (
          <div className="flex gap-3">
            <button
              onClick={() => setConfirmando('APROBADO')}
              className="flex-1 py-3 bg-green-600 hover:bg-green-700 text-white font-semibold text-sm rounded-lg transition flex items-center justify-center gap-2"
            >
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
              </svg>
              Aprobar sustentación
            </button>
            <button
              onClick={() => setConfirmando('NO_APROBADO')}
              className="flex-1 py-3 bg-red-600 hover:bg-red-700 text-white font-semibold text-sm rounded-lg transition flex items-center justify-center gap-2"
            >
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
              </svg>
              No aprobar
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
