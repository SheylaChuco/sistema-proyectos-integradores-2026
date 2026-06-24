import type { EstadoProyecto } from '../../../shared/types/proyecto.types';

const estilos: Record<EstadoProyecto, string> = {
  EN_DESARROLLO: 'bg-[#FFF7ED] text-[#C2410C] border-[#FED7AA]',
  APROBADO: 'bg-[#F0FDF4] text-[#15803D] border-[#BBF7D0]',
  NO_APROBADO: 'bg-[#FFF1F2] text-[#BE123C] border-[#FECDD3]',
};

const etiquetas: Record<EstadoProyecto, string> = {
  EN_DESARROLLO: 'En desarrollo',
  APROBADO: 'Aprobado',
  NO_APROBADO: 'No aprobado',
};

interface Props {
  estado: EstadoProyecto;
}

export default function BadgeEstado({ estado }: Props) {
  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium border ${estilos[estado]}`}>
      {etiquetas[estado]}
    </span>
  );
}
