import { useEffect, useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { listarProyectosSustentacion, type ProyectoSustentacionDto } from '../services/sustentacionService';

export default function ListaSustentacionesAdmin() {
  const [proyectos, setProyectos] = useState<ProyectoSustentacionDto[]>([]);
  const [cargando, setCargando] = useState(true);

  const cargar = useCallback(async () => {
    setCargando(true);
    try {
      setProyectos(await listarProyectosSustentacion());
    } catch {
      setProyectos([]);
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => { cargar(); }, [cargar]);

  return (
    <div className="p-8">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-[#0F172A]">Sustentaciones</h1>
        <p className="text-[#64748B] text-sm mt-1">
          Proyectos aprobados pendientes de evaluación final
        </p>
      </div>

      {cargando ? (
        <div className="space-y-3">
          {Array.from({ length: 4 }).map((_, i) => (
            <div key={i} className="bg-white rounded-xl border border-[#E2E8F0] p-5 animate-pulse">
              <div className="h-4 bg-[#E2E8F0] rounded w-1/3 mb-2" />
              <div className="h-3 bg-[#E2E8F0] rounded w-2/3" />
            </div>
          ))}
        </div>
      ) : proyectos.length === 0 ? (
        <div className="bg-white rounded-xl border border-[#E2E8F0] p-16 text-center">
          <div className="w-12 h-12 rounded-2xl bg-[#EEF2F7] flex items-center justify-center mx-auto mb-4">
            <svg className="w-6 h-6 text-[#94A3B8]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" />
            </svg>
          </div>
          <p className="text-sm font-semibold text-[#0F172A]">Sin proyectos pendientes</p>
          <p className="text-xs text-[#64748B] mt-1">No hay proyectos en desarrollo listos para evaluar.</p>
        </div>
      ) : (
        <div className="space-y-3">
          {proyectos.map((p) => (
            <Link
              key={p.id}
              to={`/admin/sustentaciones/${p.id}`}
              className="bg-white rounded-xl border border-l-4 border-[#E2E8F0] border-l-[#2563EB] p-5 flex items-start justify-between hover:shadow-md transition block"
            >
              <div className="min-w-0">
                <div className="flex items-center gap-2 mb-1">
                  <span className="text-xs font-medium text-[#64748B] bg-[#EEF2F7] px-2 py-0.5 rounded-full">
                    {p.grupo_codigo}
                  </span>
                  <span className="text-xs text-[#94A3B8]">{p.grupo_periodo}</span>
                  {p.url && (
                    <span className="text-xs text-[#2563EB] bg-blue-50 px-2 py-0.5 rounded-full">
                      Con repositorio
                    </span>
                  )}
                </div>
                <p className="font-semibold text-[#0F172A] text-sm leading-snug">{p.nombre}</p>
                <p className="text-xs text-[#64748B] mt-1 line-clamp-1">{p.descripcion}</p>
              </div>
              <svg className="w-5 h-5 text-[#94A3B8] flex-shrink-0 ml-4 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7" />
              </svg>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
