import { useEffect, useState, useMemo } from 'react';
import { listarEstudiantes, type EstudianteAdminDto } from '../services/estudianteAdminService';

type Filtro = 'todos' | 'con_grupo' | 'sin_grupo';

function Avatar({ nombre }: { nombre: string }) {
  const iniciales = nombre
    .split(' ')
    .slice(0, 2)
    .map((p) => p.charAt(0).toUpperCase())
    .join('');
  return (
    <div className="w-9 h-9 rounded-full bg-[#2563EB] flex items-center justify-center flex-shrink-0">
      <span className="text-white text-xs font-bold">{iniciales}</span>
    </div>
  );
}

export default function EstudiantesAdmin() {
  const [estudiantes, setEstudiantes] = useState<EstudianteAdminDto[]>([]);
  const [cargando, setCargando] = useState(true);
  const [filtro, setFiltro] = useState<Filtro>('todos');
  const [busqueda, setBusqueda] = useState('');

  useEffect(() => {
    listarEstudiantes()
      .then(setEstudiantes)
      .catch(() => setEstudiantes([]))
      .finally(() => setCargando(false));
  }, []);

  const conGrupo = useMemo(() => estudiantes.filter((e) => e.en_grupo).length, [estudiantes]);
  const sinGrupo = useMemo(() => estudiantes.filter((e) => !e.en_grupo).length, [estudiantes]);

  const lista = useMemo(() => {
    let result = estudiantes;
    if (filtro === 'con_grupo') result = result.filter((e) => e.en_grupo);
    if (filtro === 'sin_grupo') result = result.filter((e) => !e.en_grupo);
    if (busqueda.trim()) {
      const q = busqueda.toLowerCase();
      result = result.filter(
        (e) =>
          e.nombre.toLowerCase().includes(q) ||
          e.codigo_estudiante.toLowerCase().includes(q)
      );
    }
    return result;
  }, [estudiantes, filtro, busqueda]);

  const FILTROS: { key: Filtro; label: string }[] = [
    { key: 'todos', label: `Todos (${estudiantes.length})` },
    { key: 'con_grupo', label: `Con grupo (${conGrupo})` },
    { key: 'sin_grupo', label: `Sin grupo (${sinGrupo})` },
  ];

  return (
    <div className="p-8">
      {/* Header */}
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-[#0F172A]">Estudiantes</h1>
        <p className="text-[#64748B] text-sm mt-1">
          Lista de estudiantes registrados y su estado en grupos
        </p>
      </div>

      {/* Contadores */}
      <div className="grid grid-cols-3 gap-4 mb-6">
        {[
          { label: 'Total registrados', valor: estudiantes.length, color: 'text-[#0F172A]', bg: 'bg-white' },
          { label: 'Con grupo activo', valor: conGrupo, color: 'text-green-600', bg: 'bg-green-50 border-green-200' },
          { label: 'Sin grupo', valor: sinGrupo, color: 'text-[#64748B]', bg: 'bg-white' },
        ].map((c) => (
          <div key={c.label} className={`${c.bg} rounded-xl border border-[#E2E8F0] p-4`}>
            <p className={`text-2xl font-bold ${c.color}`}>{cargando ? '—' : c.valor}</p>
            <p className="text-xs text-[#64748B] mt-0.5">{c.label}</p>
          </div>
        ))}
      </div>

      {/* Filtros + búsqueda */}
      <div className="flex items-center gap-3 mb-4 flex-wrap">
        <div className="flex gap-1 bg-white border border-[#E2E8F0] rounded-lg p-1">
          {FILTROS.map((f) => (
            <button
              key={f.key}
              onClick={() => setFiltro(f.key)}
              className={`px-3 py-1.5 rounded-md text-xs font-medium transition ${
                filtro === f.key
                  ? 'bg-[#2563EB] text-white'
                  : 'text-[#64748B] hover:bg-[#F1F5F9]'
              }`}
            >
              {f.label}
            </button>
          ))}
        </div>

        <div className="relative flex-1 min-w-[200px]">
          <svg
            className="w-4 h-4 text-[#94A3B8] absolute left-3 top-1/2 -translate-y-1/2"
            fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}
          >
            <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
          <input
            type="text"
            value={busqueda}
            onChange={(e) => setBusqueda(e.target.value)}
            placeholder="Buscar por nombre o código..."
            className="w-full pl-9 pr-4 py-2 text-sm border border-[#E2E8F0] rounded-lg bg-white placeholder-[#94A3B8] focus:outline-none focus:ring-2 focus:ring-[#2563EB] focus:border-transparent"
          />
        </div>
      </div>

      {/* Lista */}
      {cargando ? (
        <div className="space-y-2">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="bg-white rounded-xl border border-[#E2E8F0] p-4 animate-pulse flex items-center gap-3">
              <div className="w-9 h-9 rounded-full bg-[#E2E8F0]" />
              <div className="flex-1 space-y-1.5">
                <div className="h-3.5 bg-[#E2E8F0] rounded w-40" />
                <div className="h-3 bg-[#E2E8F0] rounded w-56" />
              </div>
              <div className="h-5 bg-[#E2E8F0] rounded-full w-20" />
            </div>
          ))}
        </div>
      ) : lista.length === 0 ? (
        <div className="bg-white rounded-xl border border-[#E2E8F0] p-16 text-center">
          <div className="w-12 h-12 rounded-2xl bg-[#EEF2F7] flex items-center justify-center mx-auto mb-4">
            <svg className="w-6 h-6 text-[#94A3B8]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" />
            </svg>
          </div>
          <p className="text-sm font-semibold text-[#0F172A]">Sin resultados</p>
          <p className="text-xs text-[#64748B] mt-1">No hay estudiantes que coincidan con los filtros.</p>
        </div>
      ) : (
        <div className="space-y-2">
          {lista.map((e) => (
            <div
              key={e.id}
              className="bg-white rounded-xl border border-[#E2E8F0] px-4 py-3 flex items-center gap-3 hover:border-[#CBD5E1] transition"
            >
              <Avatar nombre={e.nombre} />

              <div className="flex-1 min-w-0">
                <p className="text-sm font-semibold text-[#0F172A] truncate">{e.nombre}</p>
                <p className="text-xs text-[#64748B] truncate">
                  {e.codigo_estudiante} · {e.correo}
                </p>
              </div>

              {e.en_grupo ? (
                <div className="flex items-center gap-2 flex-shrink-0">
                  <div className="text-right hidden sm:block">
                    <p className="text-xs font-medium text-[#0F172A]">{e.grupo_codigo}</p>
                    <p className="text-xs text-[#64748B]">{e.grupo_periodo}</p>
                  </div>
                  <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium bg-green-50 text-green-700 border border-green-200">
                    <span className="w-1.5 h-1.5 rounded-full bg-green-500" />
                    Con grupo
                  </span>
                </div>
              ) : (
                <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium bg-[#F1F5F9] text-[#64748B] border border-[#E2E8F0] flex-shrink-0">
                  <span className="w-1.5 h-1.5 rounded-full bg-[#CBD5E1]" />
                  Sin grupo
                </span>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
