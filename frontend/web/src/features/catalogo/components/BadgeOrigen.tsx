import type { OrigenProyecto } from '../../../shared/types/proyecto.types';

interface Props {
  origen: OrigenProyecto;
}

export default function BadgeOrigen({ origen }: Props) {
  if (origen === 'HISTORICO') {
    return (
      <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-[#F0F9FF] text-[#0369A1] border border-[#BAE6FD]">
        Histórico
      </span>
    );
  }
  return (
    <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-[#F0FDF4] text-[#15803D] border border-[#BBF7D0]">
      Nuevo
    </span>
  );
}
