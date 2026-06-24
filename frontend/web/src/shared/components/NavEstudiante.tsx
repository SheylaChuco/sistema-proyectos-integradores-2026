import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

const links = [
  { to: '/catalogo', label: 'Catálogo' },
  { to: '/mi-grupo', label: 'Mi Grupo' },
  { to: '/mi-propuesta', label: 'Mi Propuesta' },
  { to: '/asistente', label: 'Asistente IA' },
];

export default function NavEstudiante() {
  const { usuario, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/login', { replace: true });
  }

  return (
    <nav className="bg-white border-b border-[#E2E8F0] sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 h-14 flex items-center justify-between">
        <div className="flex items-center gap-6">
          <span className="font-bold text-[#003087] text-lg">Integratec</span>
          <div className="hidden md:flex items-center gap-1">
            {links.map((l) => (
              <NavLink
                key={l.to}
                to={l.to}
                className={({ isActive }) =>
                  `px-3 py-1.5 rounded-lg text-sm font-medium transition ${
                    isActive ? 'bg-[#EFF6FF] text-[#0057D8]' : 'text-[#64748B] hover:text-[#0F172A]'
                  }`
                }
              >
                {l.label}
              </NavLink>
            ))}
          </div>
        </div>
        <div className="flex items-center gap-3">
          <span className="text-sm text-[#64748B] hidden sm:block">{usuario?.nombre}</span>
          <button
            onClick={handleLogout}
            className="text-sm text-[#64748B] hover:text-[#DC2626] transition"
          >
            Salir
          </button>
        </div>
      </div>
    </nav>
  );
}
