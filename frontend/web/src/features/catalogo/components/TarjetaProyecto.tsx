import { Link } from 'react-router-dom';
import type { ProyectoLista } from '../../../shared/types/proyecto.types';

const badgeEstado: Record<string, string> = {
  EN_DESARROLLO: 'bg-orange-100 text-orange-700',
  APROBADO: 'bg-green-100 text-green-700',
  NO_APROBADO: 'bg-red-100 text-red-700',
  PENDIENTE: 'bg-yellow-100 text-yellow-700',
  OBSERVADO: 'bg-rose-100 text-rose-600',
};

const labelEstado: Record<string, string> = {
  EN_DESARROLLO: 'En desarrollo',
  APROBADO: 'Aprobado',
  NO_APROBADO: 'No aprobado',
  PENDIENTE: 'Pendiente',
  OBSERVADO: 'Observado',
};

interface Props {
  proyecto: ProyectoLista;
}

export default function TarjetaProyecto({ proyecto }: Props) {
  return (
    <Link
      to={`/catalogo/${proyecto.id}`}
      className="block bg-white rounded-xl border border-[#E2E8F0] shadow-sm p-5 hover:shadow-md hover:border-[#2563EB] transition"
    >
      <div className="flex items-start justify-between gap-2 mb-3">
        <span className="inline-block px-2.5 py-0.5 rounded-full text-xs font-medium bg-[#EEF2F7] text-[#64748B]">
          Ciclo {proyecto.ciclo}
        </span>
        {proyecto.origen === 'NUEVO' && proyecto.estado && (
          <span className={`inline-block px-2.5 py-0.5 rounded-full text-xs font-medium ${badgeEstado[proyecto.estado] ?? 'bg-gray-100 text-gray-600'}`}>
            {labelEstado[proyecto.estado] ?? proyecto.estado}
          </span>
        )}
      </div>

      <h3 className="font-semibold text-[#0F172A] text-sm leading-snug line-clamp-2 mb-2">
        {proyecto.nombre}
      </h3>

      <p className="text-xs text-[#2563EB] line-clamp-2 mb-4">
        {proyecto.descripcion}
      </p>

      <div className="flex items-center gap-1.5 text-xs text-[#94A3B8]">
        <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        {proyecto.codigo_seccion
          ? `${proyecto.codigo_seccion} · ${proyecto.ciclo}`
          : proyecto.codigo_grupo
          ? `${proyecto.codigo_grupo} · ${proyecto.ciclo}`
          : `Ciclo ${proyecto.ciclo}`}
      </div>
    </Link>
  );
}
