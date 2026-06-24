import { useEffect, useState } from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import NavAdmin from '../../../../shared/components/NavAdmin';
import { obtenerEstadisticas, type EstadisticasDto } from '../services/dashboardService';

interface TarjetaEstadProps {
  titulo: string;
  valor: number;
  color: string;
}

function TarjetaEstad({ titulo, valor, color }: TarjetaEstadProps) {
  return (
    <div className="bg-[#1E293B] rounded-xl border border-[#334155] p-5">
      <p className="text-xs text-[#64748B] font-medium uppercase tracking-wider mb-2">{titulo}</p>
      <p className={`text-3xl font-bold ${color}`}>{valor}</p>
    </div>
  );
}

export default function DashboardAdmin() {
  const [stats, setStats] = useState<EstadisticasDto | null>(null);
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    async function cargar() {
      try {
        const data = await obtenerEstadisticas();
        setStats(data);
      } catch {
        // estadísticas no disponibles
      } finally {
        setCargando(false);
      }
    }
    cargar();
  }, []);

  return (
    <div className="min-h-screen bg-[#0F172A]">
      <NavAdmin />
      <main className="max-w-7xl mx-auto px-4 py-8">
        <div className="mb-8">
          <h1 className="text-2xl font-bold text-white">Estadísticas</h1>
          <p className="text-[#64748B] text-sm mt-1">Resumen del sistema de proyectos integradores</p>
        </div>

        {cargando ? (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {Array.from({ length: 8 }).map((_, i) => (
              <div key={i} className="bg-[#1E293B] rounded-xl border border-[#334155] p-5 animate-pulse">
                <div className="h-3 bg-[#334155] rounded w-2/3 mb-3" />
                <div className="h-8 bg-[#334155] rounded w-1/2" />
              </div>
            ))}
          </div>
        ) : stats ? (
          <>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
              <TarjetaEstad titulo="Total proyectos" valor={stats.total_proyectos} color="text-white" />
              <TarjetaEstad titulo="Históricos" valor={stats.proyectos_historicos} color="text-[#38BDF8]" />
              <TarjetaEstad titulo="Nuevos" valor={stats.proyectos_nuevos} color="text-[#34D399]" />
              <TarjetaEstad titulo="Pendientes" valor={stats.propuestas_pendientes} color="text-[#FB923C]" />
              <TarjetaEstad titulo="Aprobados" valor={stats.propuestas_aprobadas} color="text-[#34D399]" />
              <TarjetaEstad titulo="Observados" valor={stats.propuestas_observadas} color="text-[#FBBF24]" />
              <TarjetaEstad titulo="En desarrollo" valor={stats.proyectos_en_desarrollo} color="text-[#A78BFA]" />
              <TarjetaEstad titulo="Finalizados" valor={stats.proyectos_aprobados + stats.proyectos_no_aprobados} color="text-[#94A3B8]" />
            </div>

            {stats.por_ciclo.length > 0 && (
              <div className="bg-[#1E293B] rounded-xl border border-[#334155] p-6">
                <h2 className="text-sm font-semibold text-white mb-6">Proyectos por ciclo</h2>
                <ResponsiveContainer width="100%" height={280}>
                  <BarChart data={stats.por_ciclo} margin={{ top: 0, right: 0, left: -20, bottom: 0 }}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
                    <XAxis dataKey="ciclo" tick={{ fill: '#64748B', fontSize: 12 }} />
                    <YAxis tick={{ fill: '#64748B', fontSize: 12 }} />
                    <Tooltip
                      contentStyle={{ background: '#0F172A', border: '1px solid #334155', borderRadius: 8 }}
                      labelStyle={{ color: '#94A3B8' }}
                      itemStyle={{ color: '#0057D8' }}
                    />
                    <Bar dataKey="total" fill="#0057D8" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            )}
          </>
        ) : (
          <div className="text-center py-20 text-[#64748B]">
            <p>No se pudieron cargar las estadísticas.</p>
          </div>
        )}
      </main>
    </div>
  );
}
