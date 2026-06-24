import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { listarProyectos } from '../../catalogo/services/catalogoService';
import { obtenerMiGrupo, type GrupoDto } from '../../grupos/services/grupoService';
import { obtenerMiPropuesta } from '../../propuestas/services/propuestaService';
import type { ProyectoLista } from '../../../shared/types/proyecto.types';
import type { PropuestaDto } from '../../../shared/types/propuesta.types';

const DIAS  = ['domingo','lunes','martes','miércoles','jueves','viernes','sábado'];
const MESES = ['enero','febrero','marzo','abril','mayo','junio','julio','agosto','septiembre','octubre','noviembre','diciembre'];

function fechaHoy() {
  const d = new Date();
  return `${DIAS[d.getDay()]}, ${d.getDate()} de ${MESES[d.getMonth()]} de ${d.getFullYear()}`;
}
function capitalize(s: string) { return s.charAt(0).toUpperCase() + s.slice(1); }

// ── Configuración de estados ─────────────────────────────────────────────────

interface InfoPropuesta { texto: string; colorTexto: string; colorDot: string; }
const ESTADO_PROPUESTA: Record<string, InfoPropuesta> = {
  PENDIENTE: {
    texto: 'Propuesta en revisión',
    colorTexto: 'text-[#0F172A]',
    colorDot: 'bg-blue-400',
  },
  OBSERVADO: {
    texto: 'Con observaciones — requiere corrección',
    colorTexto: 'text-orange-600',
    colorDot: 'bg-orange-400',
  },
  APROBADO: {
    texto: '¡Propuesta aprobada!',
    colorTexto: 'text-green-600',
    colorDot: 'bg-green-500',
  },
  NO_APROBADO: {
    texto: 'Propuesta no aprobada',
    colorTexto: 'text-red-600',
    colorDot: 'bg-red-400',
  },
};

// ── Íconos ───────────────────────────────────────────────────────────────────

function IconoGrupo({ activo }: { activo: boolean }) {
  return (
    <div className={`w-10 h-10 rounded-lg flex items-center justify-center ${activo ? 'bg-blue-50' : 'bg-[#EEF2F7]'}`}>
      <svg className={`w-5 h-5 ${activo ? 'text-[#2563EB]' : 'text-[#94A3B8]'}`} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" />
      </svg>
    </div>
  );
}

function IconoPropuesta({ estado }: { estado?: string }) {
  const colores: Record<string, string> = {
    PENDIENTE:   'text-[#2563EB] bg-blue-50',
    OBSERVADO:   'text-orange-500 bg-orange-50',
    APROBADO:    'text-green-600 bg-green-50',
    NO_APROBADO: 'text-red-500 bg-red-50',
  };
  const clase = estado ? (colores[estado] ?? 'text-[#64748B] bg-[#EEF2F7]') : 'text-[#94A3B8] bg-[#EEF2F7]';
  return (
    <div className={`w-10 h-10 rounded-lg flex items-center justify-center ${clase}`}>
      <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
      </svg>
    </div>
  );
}

// ── Componente principal ──────────────────────────────────────────────────────

export default function Inicio() {
  const { usuario } = useAuth();
  const [grupo, setGrupo] = useState<GrupoDto | null | undefined>(undefined);
  const [propuesta, setPropuesta] = useState<PropuestaDto | null | undefined>(undefined);
  const [recientes, setRecientes] = useState<ProyectoLista[]>([]);

  const primerNombre = usuario?.nombre?.split(' ')[0] ?? '';
  const cargando = grupo === undefined;

  useEffect(() => {
    // Fetch grupo y proyectos recientes en paralelo
    Promise.all([
      obtenerMiGrupo().catch(() => null),
      listarProyectos({ page: 0, size: 3 }).then((d) => d.content).catch(() => []),
    ]).then(([g, proyectos]) => {
      setGrupo(g);
      setRecientes(proyectos);

      // Solo buscar propuesta si hay grupo
      if (g) {
        obtenerMiPropuesta()
          .then(setPropuesta)
          .catch(() => setPropuesta(null));
      } else {
        setPropuesta(null);
      }
    });
  }, []);

  // ── Textos dinámicos tarjetas ──────────────────────────────────────────────

  const infoGrupo = grupo
    ? { texto: `Grupo ${grupo.codigo_grupo} · ${grupo.integrantes.length} integrante${grupo.integrantes.length !== 1 ? 's' : ''}`, link: '/mi-grupo', linkTexto: 'Ver grupo' }
    : { texto: 'Sin grupo registrado', link: '/mi-grupo', linkTexto: 'Registrar grupo' };

  const infoPropuesta = !grupo
    ? { texto: 'Necesitas un grupo primero', colorTexto: 'text-[#94A3B8]', link: '/mi-grupo', linkTexto: 'Registrar grupo' }
    : !propuesta
    ? { texto: 'Sin propuesta activa', colorTexto: 'text-[#0F172A]', link: '/mi-propuesta', linkTexto: 'Ver propuesta' }
    : { ...ESTADO_PROPUESTA[propuesta.estado] ?? { texto: propuesta.estado, colorTexto: 'text-[#0F172A]', colorDot: '' }, link: '/mi-propuesta', linkTexto: 'Ver propuesta' };

  // ── Esqueleto de carga ─────────────────────────────────────────────────────

  const SkeletonCard = () => (
    <div className="bg-white rounded-xl p-5 border border-[#E2E8F0] shadow-sm animate-pulse">
      <div className="h-3 bg-[#E2E8F0] rounded w-16 mb-3" />
      <div className="h-4 bg-[#E2E8F0] rounded w-3/4 mb-3" />
      <div className="h-3 bg-[#E2E8F0] rounded w-20" />
    </div>
  );

  return (
    <div className="p-8">
      {/* Saludo */}
      <h1 className="text-3xl font-bold text-[#0F172A]">Hola, {primerNombre}</h1>
      <p className="text-[#64748B] text-sm mt-1">{capitalize(fechaHoy())}</p>

      {/* Tarjetas de resumen */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-6">
        {cargando ? (
          <>
            <SkeletonCard />
            <SkeletonCard />
          </>
        ) : (
          <>
            {/* ── MI GRUPO ── */}
            <div className={`bg-white rounded-xl p-5 flex items-start justify-between border shadow-sm transition ${
              grupo ? 'border-[#E2E8F0]' : 'border-dashed border-[#CBD5E1]'
            }`}>
              <div>
                <p className="text-xs font-semibold text-[#64748B] uppercase tracking-wide mb-1">Mi Grupo</p>
                <p className={`text-base font-semibold mb-2 ${grupo ? 'text-[#0F172A]' : 'text-[#94A3B8]'}`}>
                  {infoGrupo.texto}
                </p>
                <Link
                  to={infoGrupo.link}
                  className="text-sm text-[#2563EB] hover:underline inline-flex items-center gap-1 font-medium"
                >
                  {infoGrupo.linkTexto}
                  <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7" />
                  </svg>
                </Link>
              </div>
              <IconoGrupo activo={!!grupo} />
            </div>

            {/* ── MI PROPUESTA ── */}
            <div className={`bg-white rounded-xl p-5 flex items-start justify-between border shadow-sm transition ${
              propuesta?.estado === 'OBSERVADO'
                ? 'border-orange-200'
                : propuesta?.estado === 'APROBADO'
                ? 'border-green-200'
                : propuesta?.estado === 'NO_APROBADO'
                ? 'border-red-200'
                : 'border-[#E2E8F0]'
            }`}>
              <div>
                <p className="text-xs font-semibold text-[#64748B] uppercase tracking-wide mb-1">Mi Propuesta</p>

                {/* Dot indicador de estado */}
                {'colorDot' in infoPropuesta && infoPropuesta.colorDot && (
                  <span className={`inline-block w-2 h-2 rounded-full mb-1.5 ${infoPropuesta.colorDot}`} />
                )}

                <p className={`text-base font-semibold mb-2 leading-snug ${infoPropuesta.colorTexto}`}>
                  {infoPropuesta.texto}
                </p>
                <Link
                  to={infoPropuesta.link}
                  className="text-sm text-[#2563EB] hover:underline inline-flex items-center gap-1 font-medium"
                >
                  {infoPropuesta.linkTexto}
                  <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7" />
                  </svg>
                </Link>
              </div>
              <IconoPropuesta estado={propuesta?.estado} />
            </div>
          </>
        )}
      </div>

      {/* Acciones rápidas */}
      <div className="bg-white rounded-xl p-5 mt-4 border border-[#E2E8F0] shadow-sm">
        <p className="text-sm font-semibold text-[#0F172A] mb-3">Acciones rápidas</p>
        <div className="flex flex-wrap gap-2">
          <Link
            to="/catalogo"
            className="px-4 py-2 rounded-full text-sm font-medium bg-[#F1F5F9] text-[#374151] hover:bg-[#E2E8F0] transition border border-[#CBD5E1]"
          >
            Explorar catálogo
          </Link>
          <Link
            to="/mi-grupo"
            className="px-4 py-2 rounded-full text-sm font-medium bg-[#F1F5F9] text-[#374151] hover:bg-[#E2E8F0] transition border border-[#CBD5E1]"
          >
            {grupo ? 'Mi grupo' : 'Registrar grupo'}
          </Link>
          {grupo && (
            <Link
              to="/mi-propuesta"
              className="px-4 py-2 rounded-full text-sm font-medium bg-[#F1F5F9] text-[#374151] hover:bg-[#E2E8F0] transition border border-[#CBD5E1]"
            >
              Mi propuesta
            </Link>
          )}
          <Link
            to="/asistente"
            className="px-4 py-2 rounded-full text-sm font-medium bg-[#EDE9FE] text-[#6C63FF] hover:bg-[#DDD6FE] transition border border-[#C4B5FD]"
          >
            Consultar IA
          </Link>
        </div>
      </div>

      {/* Proyectos recientes */}
      <div className="mt-6">
        <div className="flex items-center justify-between mb-3">
          <p className="text-base font-semibold text-[#0F172A]">Proyectos recientes</p>
          <Link to="/catalogo" className="text-sm text-[#2563EB] hover:underline font-medium">
            Ver catálogo completo
          </Link>
        </div>
        {recientes.length === 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="bg-white rounded-xl p-4 border border-[#E2E8F0] animate-pulse">
                <div className="h-3 bg-[#E2E8F0] rounded w-16 mb-2" />
                <div className="h-4 bg-[#E2E8F0] rounded w-full mb-2" />
                <div className="h-3 bg-[#E2E8F0] rounded w-4/5" />
              </div>
            ))}
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {recientes.map((p) => (
              <Link
                key={p.id}
                to={`/catalogo/${p.id}`}
                className="bg-white rounded-xl p-4 border border-[#E2E8F0] shadow-sm hover:shadow-md hover:border-[#2563EB] transition block"
              >
                <p className="text-xs text-[#64748B] mb-1.5">Ciclo {p.ciclo}</p>
                <p className="font-semibold text-[#0F172A] text-sm leading-snug mb-1">{p.nombre}</p>
                <p className="text-xs text-[#2563EB] line-clamp-2">{p.descripcion}</p>
              </Link>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
