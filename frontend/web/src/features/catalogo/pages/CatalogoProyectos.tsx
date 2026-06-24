import { useEffect, useState, useCallback } from 'react';
import TarjetaProyecto from '../components/TarjetaProyecto';
import { listarProyectos, obtenerCiclos, type FiltrosCatalogo } from '../services/catalogoService';
import type { ProyectoLista } from '../../../shared/types/proyecto.types';

export default function CatalogoProyectos() {
  const [proyectos, setProyectos] = useState<ProyectoLista[]>([]);
  const [cargando, setCargando] = useState(true);
  const [total, setTotal] = useState(0);
  const [totalPaginas, setTotalPaginas] = useState(0);
  const [paginaActual, setPaginaActual] = useState(0);
  const [filtros, setFiltros] = useState<FiltrosCatalogo>({ page: 0, size: 12 });
  const [busquedaInput, setBusquedaInput] = useState('');
  const [ciclosDisponibles, setCiclosDisponibles] = useState<string[]>([]);

  useEffect(() => {
    obtenerCiclos().then(setCiclosDisponibles).catch(() => {});
  }, []);

  const cargar = useCallback(async (f: FiltrosCatalogo) => {
    setCargando(true);
    try {
      const data = await listarProyectos(f);
      setProyectos(data.content);
      setTotal(data.total_elements);
      setTotalPaginas(data.total_pages);
      setPaginaActual(data.page);
    } catch {
      setProyectos([]);
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => { cargar(filtros); }, [filtros, cargar]);

  function handleBuscar(e: React.FormEvent) {
    e.preventDefault();
    setFiltros((f) => ({ ...f, busqueda: busquedaInput || undefined, page: 0 }));
  }

  return (
    <div className="p-8">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-[#0F172A]">Catálogo de proyectos</h1>
        <p className="text-sm text-[#64748B] mt-0.5">
          {cargando ? 'Cargando...' : `${total} proyectos encontrados`}
        </p>
      </div>

      {/* Filtros */}
      <div className="flex flex-wrap gap-3 mb-6">
        <form onSubmit={handleBuscar} className="flex gap-2 flex-1 min-w-[240px] max-w-lg">
          <div className="relative flex-1">
            <svg className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[#94A3B8]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
            <input
              type="text"
              placeholder="Buscar proyecto..."
              value={busquedaInput}
              onChange={(e) => setBusquedaInput(e.target.value)}
              className="w-full pl-9 pr-3 py-2.5 rounded-lg border border-[#CBD5E1] text-sm bg-white placeholder-[#94A3B8] focus:outline-none focus:ring-2 focus:ring-[#2563EB] focus:border-transparent"
            />
          </div>
          <button
            type="submit"
            className="px-4 py-2.5 bg-[#2563EB] text-white text-sm rounded-lg hover:bg-[#1D4ED8] transition"
          >
            Buscar
          </button>
        </form>

        <select
          value={filtros.ciclo ?? ''}
          onChange={(e) => setFiltros((f) => ({ ...f, ciclo: e.target.value || undefined, page: 0 }))}
          className="px-3 py-2.5 rounded-lg border border-[#CBD5E1] text-sm bg-white text-[#374151] focus:outline-none focus:ring-2 focus:ring-[#2563EB]"
        >
          <option value="">Todos los periodos</option>
          {ciclosDisponibles.map((c) => (
            <option key={c} value={c}>{c}</option>
          ))}
        </select>

        <select
          value={filtros.origen ?? ''}
          onChange={(e) => setFiltros((f) => ({ ...f, origen: (e.target.value as 'HISTORICO' | 'NUEVO') || undefined, page: 0 }))}
          className="px-3 py-2.5 rounded-lg border border-[#CBD5E1] text-sm bg-white text-[#374151] focus:outline-none focus:ring-2 focus:ring-[#2563EB]"
        >
          <option value="">Todos</option>
          <option value="HISTORICO">Históricos</option>
          <option value="NUEVO">Nuevos</option>
        </select>
      </div>

      {/* Grid */}
      {cargando ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="bg-white rounded-xl border border-[#E2E8F0] p-5 animate-pulse">
              <div className="h-3 bg-[#E2E8F0] rounded-full w-16 mb-3" />
              <div className="h-4 bg-[#E2E8F0] rounded w-full mb-2" />
              <div className="h-4 bg-[#E2E8F0] rounded w-4/5 mb-4" />
              <div className="h-3 bg-[#E2E8F0] rounded w-24" />
            </div>
          ))}
        </div>
      ) : proyectos.length === 0 ? (
        <div className="text-center py-20 text-[#64748B]">
          <p className="text-lg font-medium">No se encontraron proyectos</p>
          <p className="text-sm mt-1">Intenta con otros criterios de búsqueda</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {proyectos.map((p) => <TarjetaProyecto key={p.id} proyecto={p} />)}
        </div>
      )}

      {/* Paginación */}
      {totalPaginas > 1 && (
        <div className="flex justify-center items-center gap-3 mt-8">
          <button
            disabled={paginaActual === 0 || cargando}
            onClick={() => setFiltros((f) => ({ ...f, page: paginaActual - 1 }))}
            className="px-4 py-2 text-sm rounded-lg border border-[#CBD5E1] bg-white disabled:opacity-40 hover:bg-[#F8FAFC] transition"
          >
            ← Anterior
          </button>

          <span className="inline-flex items-center gap-1.5 px-4 py-2 rounded-lg bg-[#2563EB] text-white text-sm font-semibold">
            Página {paginaActual + 1}
            <span className="text-blue-200 font-normal">de {totalPaginas}</span>
          </span>

          <button
            disabled={paginaActual >= totalPaginas - 1 || cargando}
            onClick={() => setFiltros((f) => ({ ...f, page: paginaActual + 1 }))}
            className="px-4 py-2 text-sm rounded-lg border border-[#CBD5E1] bg-white disabled:opacity-40 hover:bg-[#F8FAFC] transition"
          >
            Siguiente →
          </button>
        </div>
      )}
    </div>
  );
}
