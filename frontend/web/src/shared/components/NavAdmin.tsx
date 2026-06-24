import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

const links = [
  { to: '/admin/propuestas', label: 'Propuestas' },
  { to: '/admin/dashboard', label: 'Estadísticas' },
];

export default function NavAdmin() {
  const { usuario, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/admin/login', { replace: true });
  }

  return (
    <nav className="bg-[#0F172A] border-b border-[#1E293B] sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 h-14 flex items-center justify-between">
        <div className="flex items-center gap-6">
          <span className="font-bold text-white text-lg">Integratec <span className="text-[#64748B] text-sm font-normal">Admin</span></span>
          <div className="flex items-center gap-1">
            {links.map((l) => (
              <NavLink
                key={l.to}
                to={l.to}
                className={({ isActive }) =>
                  `px-3 py-1.5 rounded-lg text-sm font-medium transition ${
                    isActive ? 'bg-[#1E293B] text-white' : 'text-[#94A3B8] hover:text-white'
                  }`
                }
              >
                {l.label}
              </NavLink>
            ))}
          </div>
        </div>
        <div className="flex items-center gap-3">
          <span className="text-sm text-[#64748B]">{usuario?.nombre}</span>
          <button onClick={handleLogout} className="text-sm text-[#64748B] hover:text-red-400 transition">
            Salir
          </button>
        </div>
      </div>
    </nav>
  );
}
