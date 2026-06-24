import { useEffect, useState } from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, Legend } from 'recharts';
import { obtenerEstadisticas, type EstadisticasDto } from '../services/dashboardService';

const COLORES_ESTADO = ['#2563EB', '#22C55E', '#EF4444'];
const ESTADOS_LABELS = ['En desarrollo', 'Aprobados', 'No aprobados'];

interface TarjetaProps {
  titulo: string;
  valor: number;
  colorValor?: string;
}

function Tarjeta({ titulo, valor, colorValor = 'text-[#0F172A]' }: TarjetaProps) {
  return (
    <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm p-5">
      <p className="text-xs font-semibold text-[#64748B] uppercase tracking-wide mb-2">{titulo}</p>
      <p className={`text-3xl font-bold ${colorValor}`}>{valor}</p>
    </div>
  );
}

export default function DashboardAdmin() {
  const [stats, setStats] = useState<EstadisticasDto | null>(null);
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    obtenerEstadisticas()
      .then(setStats)
      .catch(() => {})
      .finally(() => setCargando(false));
  }, []);

  const dataTorta = stats
    ? [
        { name: 'En desarrollo', value: stats.proyectos_en_desarrollo },
        { name: 'Aprobados',     value: stats.proyectos_aprobados },
        { name: 'No aprobados',  value: stats.proyectos_no_aprobados },
      ].filter((d) => d.value > 0)
    : [];

  return (
    <div className="p-8">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-[#0F172A]">Estadísticas</h1>
        <p className="text-[#64748B] text-sm mt-1">Resumen general del sistema de proyectos integradores</p>
      </div>

      {cargando ? (
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {Array.from({ length: 8 }).map((_, i) => (
            <div key={i} className="bg-white rounded-xl border border-[#E2E8F0] p-5 animate-pulse">
              <div className="h-3 bg-[#E2E8F0] rounded w-2/3 mb-3" />
              <div className="h-8 bg-[#E2E8F0] rounded w-1/2" />
            </div>
          ))}
        </div>
      ) : !stats ? (
        <div className="bg-white rounded-xl border border-[#E2E8F0] p-16 text-center text-[#94A3B8] text-sm">
          No se pudieron cargar las estadísticas.
        </div>
      ) : (
        <>
          {/* Tarjetas */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
            <Tarjeta titulo="Total proyectos"   valor={stats.total_proyectos} />
            <Tarjeta titulo="Históricos"        valor={stats.proyectos_historicos} colorValor="text-[#2563EB]" />
            <Tarjeta titulo="Nuevos"            valor={stats.proyectos_nuevos}     colorValor="text-[#16A34A]" />
            <Tarjeta titulo="Pendientes"        valor={stats.propuestas_pendientes} colorValor="text-amber-500" />
            <Tarjeta titulo="Aprobadas"         valor={stats.propuestas_aprobadas} colorValor="text-[#16A34A]" />
            <Tarjeta titulo="Observadas"        valor={stats.propuestas_observadas} colorValor="text-orange-500" />
            <Tarjeta titulo="En desarrollo"     valor={stats.proyectos_en_desarrollo} colorValor="text-[#2563EB]" />
            <Tarjeta titulo="Finalizados"       valor={stats.proyectos_aprobados + stats.proyectos_no_aprobados} colorValor="text-[#64748B]" />
          </div>

          {/* Gráficos */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
            {/* Barras — proyectos por ciclo */}
            {stats.por_ciclo.length > 0 && (
              <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm p-6">
                <p className="text-sm font-semibold text-[#0F172A] mb-5">Proyectos por ciclo</p>
                <ResponsiveContainer width="100%" height={240}>
                  <BarChart data={stats.por_ciclo} margin={{ top: 0, right: 0, left: -20, bottom: 0 }}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#E2E8F0" />
                    <XAxis dataKey="ciclo" tick={{ fill: '#64748B', fontSize: 11 }} />
                    <YAxis tick={{ fill: '#64748B', fontSize: 11 }} />
                    <Tooltip
                      contentStyle={{ background: '#fff', border: '1px solid #E2E8F0', borderRadius: 8, fontSize: 12 }}
                      labelStyle={{ color: '#0F172A', fontWeight: 600 }}
                    />
                    <Bar dataKey="total" fill="#2563EB" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            )}

            {/* Torta — proyectos nuevos por estado */}
            {dataTorta.length > 0 && (
              <div className="bg-white rounded-xl border border-[#E2E8F0] shadow-sm p-6">
                <p className="text-sm font-semibold text-[#0F172A] mb-5">Proyectos nuevos por estado</p>
                <ResponsiveContainer width="100%" height={240}>
                  <PieChart>
                    <Pie
                      data={dataTorta}
                      cx="50%"
                      cy="50%"
                      innerRadius={60}
                      outerRadius={90}
                      paddingAngle={3}
                      dataKey="value"
                    >
                      {dataTorta.map((_, i) => (
                        <Cell key={i} fill={COLORES_ESTADO[i % COLORES_ESTADO.length]} />
                      ))}
                    </Pie>
                    <Tooltip
                      contentStyle={{ background: '#fff', border: '1px solid #E2E8F0', borderRadius: 8, fontSize: 12 }}
                    />
                    <Legend iconType="circle" iconSize={8} wrapperStyle={{ fontSize: 12, color: '#64748B' }} />
                  </PieChart>
                </ResponsiveContainer>
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
}
