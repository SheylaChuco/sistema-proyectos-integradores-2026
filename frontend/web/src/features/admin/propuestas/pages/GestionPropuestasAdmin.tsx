import { useEffect, useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { listarPropuestas, type FiltrosPropuestas } from '../services/propuestaAdminService';
import type { PropuestaAdminDto } from '../../../../shared/types/propuesta.types';

const TABS = [
  { label: 'Todas',        valor: '' },
  { label: 'Pendientes',   valor: 'PENDIENTE' },
  { label: 'Aprobadas',    valor: 'APROBADO' },
  { label: 'Observadas',   valor: 'OBSERVADO' },
  { label: 'No aprobadas', valor: 'NO_APROBADO' },
];

const badgeConfig: Record<string, { label: string; clase: string; borde: string }> = {
  PENDIENTE:   { label: 'Pendiente',   clase: 'bg-yellow-100 text-yellow-700', borde: 'border-l-yellow-400' },
  APROBADO:    { label: 'Aprobado',    clase: 'bg-green-100 text-green-700',   borde: 'border-l-green-500'  },
  OBSERVADO:   { label: 'Observado',   clase: 'bg-orange-100 text-orange-700', borde: 'border-l-orange-400' },
  NO_APROBADO: { label: 'No aprobado', clase: 'bg-red-100 text-red-600',       borde: 'border-l-red-500'    },
};

export default function GestionPropuestasAdmin() {
  const [propuestas, setPropuestas] = useState<PropuestaAdminDto[]>([]);
  const [todas, setTodas] = useState<PropuestaAdminDto[]>([]);
  const [cargando, setCargando] = useState(true);
  const [tabActivo, setTabActivo] = useState('');
  const [filtros, setFiltros] = useState<FiltrosPropuestas>({});

  const cargar = useCallback(async (f: FiltrosPropuestas) => {
    setCargando(true);
    try {
      const data = await listarPropuestas(f);
      setPropuestas(Array.isArray(data) ? data : []);
      if (!f.estado) setTodas(Array.isArray(data) ? data : []);
    } catch {
      setPropuestas([]);
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => { cargar(filtros); }, [filtros, cargar]);

  function cambiarTab(valor: string) {
    setTabActivo(valor);
    setFiltros(valor ? { estado: valor } : {});
  }

  function contarEstado(estado: string) {
    return todas.filter((p) => p.estado === estado).length;
  }

  function tabLabel(tab: { label: string; valor: string }) {
    if (!tab.valor) return `${tab.label} (${todas.length})`;
    const n = contarEstado(tab.valor);
    return n > 0 ? `${tab.label} (${n})` : tab.label;
  }

  return (
    <div className="p-8">
      <div className="mb-1">
        <h1 className="text-2xl font-bold text-[#0F172A]">Propuestas</h1>
        <p className="text-sm text-[#64748B] mt-0.5">
          {cargando ? 'Cargando...' : `${propuestas.length} propuesta${propuestas.length !== 1 ? 's' : ''}`}
        </p>
      </div>

      {/* Tabs */}
      <div className="flex gap-1 border-b border-[#E2E8F0] mt-4 mb-5 overflow-x-auto">
        {TABS.map((t) => (
          <button
            key={t.valor}
            onClick={() => cambiarTab(t.valor)}
            className={`px-4 py-2.5 text-sm font-medium border-b-2 -mb-px transition whitespace-nowrap ${
              tabActivo === t.valor
                ? 'border-[#2563EB] text-[#2563EB]'
                : 'border-transparent text-[#64748B] hover:text-[#374151]'
            }`}
          >
            {tabLabel(t)}
          </button>
        ))}
      </div>

      {/* Lista */}
      {cargando ? (
        <div className="space-y-2">
          {Array.from({ length: 5 }).map((_, i) => (
            <div key={i} className="bg-white rounded-xl border border-[#E2E8F0] p-4 animate-pulse border-l-4 border-l-[#E2E8F0]">
              <div className="h-4 bg-[#E2E8F0] rounded w-1/3 mb-2" />
              <div className="h-3 bg-[#E2E8F0] rounded w-full" />
            </div>
          ))}
        </div>
      ) : propuestas.length === 0 ? (
        <div className="text-center py-20 text-[#64748B]">
          <p className="font-medium">No hay propuestas en esta categoría</p>
        </div>
      ) : (
        <div className="space-y-2">
          {propuestas.map((p) => {
            const cfg = badgeConfig[p.estado] ?? { label: p.estado, clase: 'bg-gray-100 text-gray-600', borde: 'border-l-gray-400' };
            return (
              <div
                key={p.id}
                className={`bg-white rounded-xl border border-[#E2E8F0] border-l-4 ${cfg.borde} p-4 flex items-center justify-between gap-4 hover:shadow-sm transition`}
              >
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 mb-1 flex-wrap">
                    <span className="text-xs font-bold text-[#64748B] bg-[#EEF2F7] px-2 py-0.5 rounded">
                      {p.grupo_codigo}
                    </span>
                    <span className={`px-2.5 py-0.5 rounded-full text-xs font-semibold ${cfg.clase}`}>
                      {cfg.label}
                    </span>
                  </div>
                  <p className="text-sm font-semibold text-[#0F172A] truncate">{p.nombre}</p>
                  <p className="text-xs text-[#64748B] mt-0.5">
                    {p.integrantes?.[0]?.nombre ?? '—'} · {p.grupo_periodo}
                  </p>
                </div>
                <div className="flex items-center gap-4 flex-shrink-0">
                  <span className="text-xs text-[#94A3B8]">
                    {new Date(p.fecha_envio).toLocaleDateString('es-PE', { day: 'numeric', month: 'short' })}
                  </span>
                  <Link
                    to={`/admin/propuestas/${p.id}`}
                    className="px-4 py-1.5 bg-[#2563EB] hover:bg-[#1D4ED8] text-white text-xs font-semibold rounded-lg transition"
                  >
                    Ver detalle
                  </Link>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
