import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { obtenerProyecto } from '../services/catalogoService';
import type { ProyectoDetalle } from '../../../shared/types/proyecto.types';

const labelEstado: Record<string, string> = {
  EN_DESARROLLO: 'En desarrollo',
  APROBADO: 'Aprobado',
  NO_APROBADO: 'No aprobado',
};

const badgeEstado: Record<string, string> = {
  EN_DESARROLLO: 'bg-orange-100 text-orange-700',
  APROBADO: 'bg-green-100 text-green-700',
  NO_APROBADO: 'bg-red-100 text-red-700',
};

export default function DetalleProyecto() {
  const { id } = useParams<{ id: string }>();
  const [proyecto, setProyecto] = useState<ProyectoDetalle | null>(null);
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    obtenerProyecto(Number(id))
      .then(setProyecto)
      .catch(() => {})
      .finally(() => setCargando(false));
  }, [id]);

  if (cargando) {
    return (
      <div className="p-8">
        <div className="h-4 bg-[#E2E8F0] rounded w-32 mb-6 animate-pulse" />
        <div className="bg-white rounded-xl border border-[#E2E8F0] p-6 animate-pulse">
          <div className="h-6 bg-[#E2E8F0] rounded w-2/3 mb-4" />
          <div className="h-4 bg-[#E2E8F0] rounded w-full mb-2" />
          <div className="h-4 bg-[#E2E8F0] rounded w-4/5" />
        </div>
      </div>
    );
  }

  if (!proyecto) {
    return (
      <div className="p-8">
        <Link to="/catalogo" className="text-sm text-[#64748B] hover:text-[#0F172A] flex items-center gap-1 mb-6">
          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
          </svg>
          Volver al catálogo
        </Link>
        <p className="text-[#64748B]">No se encontró el proyecto.</p>
      </div>
    );
  }

  return (
    <div className="p-8 max-w-4xl">
      <Link to="/catalogo" className="text-sm text-[#64748B] hover:text-[#0F172A] flex items-center gap-1 mb-6 w-fit">
        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
        </svg>
        Volver al catálogo
      </Link>

      <div className="bg-white rounded-xl border border-[#E2E8F0] p-6">
        {/* Badges + Sección */}
        <div className="flex items-start justify-between gap-4 mb-4">
          <div className="flex items-center gap-2 flex-wrap">
            <span className="px-2.5 py-0.5 rounded-full text-xs font-medium bg-[#EEF2F7] text-[#64748B]">
              Ciclo {proyecto.ciclo}
            </span>
            {proyecto.ciclo && (
              <span className="px-2.5 py-0.5 rounded-full text-xs font-medium bg-[#EEF2F7] text-[#64748B]">
                {proyecto.ciclo}
              </span>
            )}
            {proyecto.origen === 'NUEVO' && proyecto.estado && (
              <span className={`px-2.5 py-0.5 rounded-full text-xs font-medium ${badgeEstado[proyecto.estado]}`}>
                {labelEstado[proyecto.estado]}
              </span>
            )}
          </div>
          {(proyecto.codigo_seccion || proyecto.codigo_grupo) && (
            <div className="text-right flex-shrink-0">
              <p className="text-xs text-[#94A3B8] font-semibold uppercase tracking-wide">Sección / Grupo</p>
              <p className="text-sm font-bold text-[#0B2B5C] mt-0.5">
                {proyecto.codigo_seccion ?? proyecto.codigo_grupo}
              </p>
            </div>
          )}
        </div>

        <h1 className="text-xl font-bold text-[#0F172A] mb-3">{proyecto.nombre}</h1>
        <p className="text-sm text-[#374151] leading-relaxed">{proyecto.descripcion}</p>

        {/* Integrantes — solo NUEVO */}
        {proyecto.origen === 'NUEVO' && proyecto.integrantes && proyecto.integrantes.length > 0 && (
          <div className="mt-6 pt-5 border-t border-[#E2E8F0]">
            <p className="text-xs font-semibold text-[#64748B] uppercase tracking-wide mb-3">Integrantes</p>
            <ul className="space-y-2">
              {proyecto.integrantes.map((i) => (
                <li key={i.id} className="flex items-center gap-3">
                  <div className="w-8 h-8 rounded-full bg-[#EEF2F7] flex items-center justify-center text-[#2563EB] text-xs font-bold">
                    {i.nombre.charAt(0)}
                  </div>
                  <div>
                    <p className="text-sm font-medium text-[#0F172A]">{i.nombre}</p>
                    <p className="text-xs text-[#64748B]">{i.codigo_estudiante}</p>
                  </div>
                </li>
              ))}
            </ul>
          </div>
        )}

        {/* URL — solo NUEVO */}
        {proyecto.origen === 'NUEVO' && proyecto.url && (
          <div className="mt-5 pt-5 border-t border-[#E2E8F0]">
            <p className="text-xs font-semibold text-[#64748B] uppercase tracking-wide mb-2">Repositorio / Entrega</p>
            <a href={proyecto.url} target="_blank" rel="noopener noreferrer"
              className="text-sm text-[#2563EB] hover:underline break-all">{proyecto.url}</a>
          </div>
        )}

        {/* Comentario evaluación — solo NUEVO */}
        {proyecto.origen === 'NUEVO' && proyecto.comentario_evaluacion && (
          <div className="mt-5 pt-5 border-t border-[#E2E8F0]">
            <p className="text-xs font-semibold text-[#64748B] uppercase tracking-wide mb-2">Comentario de evaluación</p>
            <p className="text-sm text-[#374151] bg-[#F8FAFC] rounded-lg p-3 border border-[#E2E8F0]">
              {proyecto.comentario_evaluacion}
            </p>
          </div>
        )}
      </div>
    </div>
  );
}
