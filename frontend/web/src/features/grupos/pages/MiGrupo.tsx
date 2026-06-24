import { useEffect, useState, useCallback, useRef } from 'react';
import { Link } from 'react-router-dom';
import {
  obtenerMiGrupo, buscarEstudiantes, registrarGrupo,
  type GrupoDto, type BuscarEstudianteDto,
} from '../services/grupoService';
import type { ApiErrorResponse } from '../../../shared/types/api.types';

const MAX_INTEGRANTES = 4;

export default function MiGrupo() {
  const [grupo, setGrupo] = useState<GrupoDto | null>(null);
  const [cargando, setCargando] = useState(true);
  const [busqueda, setBusqueda] = useState('');
  const [resultados, setResultados] = useState<BuscarEstudianteDto[]>([]);
  const [buscando, setBuscando] = useState(false);
  const [seleccionados, setSeleccionados] = useState<BuscarEstudianteDto[]>([]);
  const [registrando, setRegistrando] = useState(false);
  const [errorApi, setErrorApi] = useState('');
  const [mostrarResultados, setMostrarResultados] = useState(false);
  const [debounceFocusTrigger, setDebounceFocusTrigger] = useState(0);

  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const inputRef = useRef<HTMLInputElement>(null);
  // Un solo ref para el contenedor (input + dropdown) — evita el bug de ref doble
  const containerRef = useRef<HTMLDivElement>(null);

  const cargar = useCallback(async () => {
    setCargando(true);
    const data = await obtenerMiGrupo();
    setGrupo(data);
    setCargando(false);
  }, []);

  useEffect(() => { cargar(); }, [cargar]);

  // Búsqueda con debounce de 350 ms
  useEffect(() => {
    if (debounceRef.current) clearTimeout(debounceRef.current);

    if (!busqueda.trim() || busqueda.trim().length < 2) {
      setResultados([]);
      setMostrarResultados(false);
      return;
    }

    debounceRef.current = setTimeout(async () => {
      setBuscando(true);
      try {
        const data = await buscarEstudiantes(busqueda.trim());
        setResultados(data);
        setMostrarResultados(true);
      } catch {
        setResultados([]);
      } finally {
        setBuscando(false);
      }
    }, 350);

    return () => { if (debounceRef.current) clearTimeout(debounceRef.current); };
  }, [busqueda]);

  // Cierra el dropdown al hacer clic fuera del contenedor completo
  useEffect(() => {
    function handleMouseDown(e: MouseEvent) {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setMostrarResultados(false);
      }
    }
    document.addEventListener('mousedown', handleMouseDown);
    return () => document.removeEventListener('mousedown', handleMouseDown);
  }, []);

  // Devuelve el foco al input DESPUÉS de que React terminó el render (evita el crash de removeChild)
  useEffect(() => {
    if (debounceFocusTrigger > 0) {
      inputRef.current?.focus();
    }
  }, [debounceFocusTrigger]);

  function toggleSeleccion(est: BuscarEstudianteDto) {
    if (est.tiene_grupo) return;
    const yaSeleccionado = seleccionados.some((s) => s.id === est.id);

    if (yaSeleccionado) {
      setSeleccionados((prev) => prev.filter((s) => s.id !== est.id));
    } else {
      if (seleccionados.length >= MAX_INTEGRANTES - 1) return;
      setSeleccionados((prev) => [...prev, est]);
      setBusqueda('');
      setResultados([]);
      setMostrarResultados(false);
      // Foco diferido para no correr durante el ciclo de commit de React
      setDebounceFocusTrigger((n) => n + 1);
    }
  }

  function quitarSeleccionado(id: number) {
    setSeleccionados((prev) => prev.filter((s) => s.id !== id));
  }

  async function handleRegistrar() {
    setErrorApi('');
    setRegistrando(true);
    try {
      const data = await registrarGrupo({ integrante_ids: seleccionados.map((s) => s.id) });
      setGrupo(data);
    } catch (e) {
      const err = e as ApiErrorResponse;
      setErrorApi(err.error?.message ?? 'Error al registrar el grupo.');
    } finally {
      setRegistrando(false);
    }
  }

  const limiteAlcanzado = seleccionados.length >= MAX_INTEGRANTES - 1;
  const totalConYo = seleccionados.length + 1;

  if (cargando) {
    return (
      <div className="p-8">
        <div className="h-7 bg-[#E2E8F0] rounded w-32 mb-6 animate-pulse" />
        <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm p-6 animate-pulse">
          <div className="h-5 bg-[#E2E8F0] rounded w-1/3 mb-4" />
          <div className="h-4 bg-[#E2E8F0] rounded w-full" />
        </div>
      </div>
    );
  }

  return (
    <div className="p-8 max-w-2xl">
      <h1 className="text-2xl font-bold text-[#0F172A] mb-6">Mi Grupo</h1>

      {/* ── Con grupo ── */}
      {grupo ? (
        <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm p-6">
          <div className="flex items-start justify-between mb-5">
            <div>
              <span className="inline-block px-2.5 py-0.5 rounded-full text-xs font-semibold bg-green-100 text-green-700 mb-2">
                Activo
              </span>
              <h2 className="text-lg font-bold text-[#0F172A]">Grupo {grupo.codigo_grupo}</h2>
              <p className="text-sm text-[#64748B]">{grupo.periodo}</p>
            </div>
            <div className="border border-[#E2E8F0] rounded-lg px-4 py-2 text-center">
              <p className="text-xs text-[#94A3B8] font-semibold uppercase tracking-wide">Código</p>
              <p className="text-base font-bold text-[#0B2B5C] mt-0.5">{grupo.codigo_grupo}</p>
            </div>
          </div>

          <p className="text-xs font-semibold text-[#64748B] uppercase tracking-wide mb-3">
            Integrantes ({grupo.integrantes.length})
          </p>
          <div className="space-y-px border border-[#E2E8F0] rounded-xl overflow-hidden">
            {grupo.integrantes.map((i) => (
              <div key={i.id} className="flex items-center gap-3 px-4 py-3 bg-white hover:bg-[#F8FAFC]">
                <div className="w-9 h-9 rounded-full bg-[#2563EB] flex items-center justify-center text-white text-sm font-bold flex-shrink-0">
                  {i.nombre.charAt(0)}
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-[#0F172A]">{i.nombre}</p>
                  <p className="text-xs text-[#64748B]">{i.codigo_estudiante}</p>
                </div>
                {i.es_lider && (
                  <span className="text-xs text-[#2563EB] font-medium bg-blue-50 px-2 py-0.5 rounded-full">
                    Tú · Líder
                  </span>
                )}
              </div>
            ))}
          </div>

          <Link
            to="/mi-propuesta"
            className="mt-5 block w-full py-3 bg-[#2563EB] hover:bg-[#1D4ED8] text-white text-center font-semibold text-sm rounded-lg transition"
          >
            Ver mi propuesta →
          </Link>
        </div>
      ) : (
        /* ── Sin grupo ── */
        <>
          <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm p-7 text-center mb-4">
            <div className="w-14 h-14 rounded-2xl bg-[#EEF2F7] flex items-center justify-center mx-auto mb-4">
              <svg className="w-7 h-7 text-[#2563EB]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
            </div>
            <h2 className="text-base font-semibold text-[#0F172A]">Aún no tienes grupo</h2>
            <p className="text-sm text-[#64748B] mt-1 max-w-xs mx-auto">
              Busca a tus compañeros y forma tu equipo para registrar una propuesta de proyecto integrador.
            </p>
          </div>

          <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm p-6">
            <h2 className="text-base font-semibold text-[#0F172A] mb-1">Registrar nuevo grupo</h2>
            <p className="text-xs text-[#94A3B8] mb-5">
              Tu grupo puede tener entre 2 y {MAX_INTEGRANTES} integrantes (incluyéndote a ti).
            </p>

            {errorApi && (
              <div className="mb-4 p-3 rounded-lg bg-red-50 border border-red-200 text-sm text-red-600">
                {errorApi}
              </div>
            )}

            {/* Buscador con autocomplete */}
            <div className="mb-5">
              <label className="block text-sm font-medium text-[#374151] mb-1.5">
                Agregar compañeros
              </label>

              {/*
                containerRef envuelve input + dropdown.
                translate="no" en el dropdown evita que Chrome inyecte nodos <Text>
                al traducir la página, lo que causaría removeChild crashes en React.
              */}
              <div ref={containerRef} className="relative">
                <div className="relative">
                  <svg className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[#94A3B8]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                  </svg>
                  {buscando && (
                    <svg className="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[#94A3B8] animate-spin" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                    </svg>
                  )}
                  <input
                    ref={inputRef}
                    type="text"
                    autoComplete="off"
                    placeholder={limiteAlcanzado ? 'Límite de integrantes alcanzado' : 'Escribe el nombre del compañero...'}
                    value={busqueda}
                    onChange={(e) => setBusqueda(e.target.value)}
                    onFocus={() => { if (resultados.length > 0) setMostrarResultados(true); }}
                    disabled={limiteAlcanzado}
                    className={`w-full pl-9 pr-9 py-2.5 rounded-lg border text-sm transition focus:outline-none focus:ring-2 focus:ring-[#2563EB] focus:border-transparent ${
                      limiteAlcanzado
                        ? 'bg-[#F8FAFC] border-[#E2E8F0] text-[#94A3B8] cursor-not-allowed'
                        : 'bg-white border-[#CBD5E1] placeholder-[#94A3B8]'
                    }`}
                  />
                </div>

                {/* Dropdown — translate="no" previene el crash de browser-translation */}
                {mostrarResultados && (
                  <div
                    translate="no"
                    className="absolute top-full left-0 right-0 mt-1 bg-white border border-[#E2E8F0] rounded-xl shadow-lg z-10 overflow-hidden max-h-60 overflow-y-auto"
                  >
                    {resultados.length > 0 ? (
                      resultados.map((est) => {
                        const yaSeleccionado = seleccionados.some((s) => s.id === est.id);
                        const deshabilitado = est.tiene_grupo && !yaSeleccionado;

                        return (
                          <button
                            key={est.id}
                            type="button"
                            onMouseDown={(e) => {
                              // preventDefault evita que el input pierda el foco antes del click
                              e.preventDefault();
                              toggleSeleccion(est);
                            }}
                            disabled={deshabilitado}
                            className={`w-full flex items-center justify-between px-4 py-3 text-left transition border-b border-[#F1F5F9] last:border-0 ${
                              deshabilitado
                                ? 'opacity-50 cursor-not-allowed'
                                : yaSeleccionado
                                ? 'bg-[#EEF6FF] hover:bg-[#E0EDFF] cursor-pointer'
                                : 'hover:bg-[#F8FAFC] cursor-pointer'
                            }`}
                          >
                            <div className="flex items-center gap-3 min-w-0">
                              <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold flex-shrink-0 ${
                                yaSeleccionado ? 'bg-[#2563EB] text-white' : 'bg-[#EEF2F7] text-[#64748B]'
                              }`}>
                                {est.nombre.charAt(0)}
                              </div>
                              <div className="min-w-0">
                                <p className="text-sm font-medium text-[#0F172A] truncate">{est.nombre}</p>
                                <p className="text-xs text-[#64748B]">{est.codigo_estudiante}</p>
                              </div>
                            </div>
                            <span className="flex-shrink-0 ml-3">
                              {est.tiene_grupo ? (
                                <span className="text-xs font-medium text-[#94A3B8] bg-[#F1F5F9] px-2 py-0.5 rounded-full">
                                  Con grupo
                                </span>
                              ) : yaSeleccionado ? (
                                <span className="text-xs font-semibold text-[#2563EB]">✓</span>
                              ) : null}
                            </span>
                          </button>
                        );
                      })
                    ) : !buscando && busqueda.trim().length >= 2 ? (
                      <div className="px-4 py-3 text-sm text-[#94A3B8]">
                        No se encontraron estudiantes
                      </div>
                    ) : null}
                  </div>
                )}
              </div>

              {limiteAlcanzado ? (
                <p className="mt-2 text-xs font-medium text-amber-600 flex items-center gap-1">
                  <svg className="w-3.5 h-3.5 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v2m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  Límite de {MAX_INTEGRANTES} integrantes alcanzado. Retira uno para agregar otro.
                </p>
              ) : (
                <p className="mt-1.5 text-xs text-[#94A3B8]">
                  Escribe al menos 2 letras ·{' '}
                  {MAX_INTEGRANTES - 1 - seleccionados.length} compañero{MAX_INTEGRANTES - 1 - seleccionados.length !== 1 ? 's' : ''} más disponible{MAX_INTEGRANTES - 1 - seleccionados.length !== 1 ? 's' : ''}
                </p>
              )}
            </div>

            {/* Integrantes seleccionados */}
            {seleccionados.length > 0 && (
              <div className="mb-5">
                <p className="text-xs font-semibold text-[#374151] uppercase tracking-wide mb-2">
                  Tu grupo ({totalConYo} de {MAX_INTEGRANTES})
                </p>
                <div className="border border-[#E2E8F0] rounded-xl overflow-hidden">
                  {/* Fila "Tú" */}
                  <div className="flex items-center gap-3 px-4 py-3 bg-[#F8FAFC] border-b border-[#E2E8F0]">
                    <div className="w-8 h-8 rounded-full bg-[#2563EB] flex items-center justify-center text-white text-xs font-bold flex-shrink-0">
                      Tú
                    </div>
                    <p className="text-sm font-medium text-[#0F172A] flex-1">Tú (creador del grupo)</p>
                    <span className="text-xs text-[#2563EB] font-medium bg-blue-50 px-2 py-0.5 rounded-full flex-shrink-0">
                      Líder
                    </span>
                  </div>

                  {/* Compañeros seleccionados */}
                  {seleccionados.map((s) => (
                    <div key={s.id} className="flex items-center gap-3 px-4 py-3 bg-white border-b border-[#E2E8F0] last:border-0">
                      <div className="w-8 h-8 rounded-full bg-[#EEF2F7] flex items-center justify-center text-[#2563EB] text-sm font-bold flex-shrink-0">
                        {s.nombre.charAt(0)}
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium text-[#0F172A]">{s.nombre}</p>
                        <p className="text-xs text-[#64748B]">{s.codigo_estudiante}</p>
                      </div>
                      <button
                        type="button"
                        onClick={() => quitarSeleccionado(s.id)}
                        className="w-7 h-7 rounded-full flex items-center justify-center text-[#94A3B8] hover:text-red-500 hover:bg-red-50 transition flex-shrink-0 text-lg leading-none"
                        title="Quitar del grupo"
                      >
                        ×
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            )}

            <button
              type="button"
              onClick={handleRegistrar}
              disabled={registrando || seleccionados.length === 0}
              className="w-full py-3 bg-[#2563EB] hover:bg-[#1D4ED8] text-white font-semibold text-sm rounded-lg transition disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {registrando
                ? 'Registrando...'
                : seleccionados.length === 0
                ? 'Agrega al menos un compañero para continuar'
                : `Registrar grupo (${totalConYo} integrante${totalConYo !== 1 ? 's' : ''})`}
            </button>
          </div>
        </>
      )}
    </div>
  );
}
