import { useApi } from '../hooks/useApi';
import { api } from '../api';
import { KpiCard } from '../components/KpiCard';
import { LineChart } from '../components/Chart/LineChart';
import { DonutChart } from '../components/Chart/DonutChart';
import { BarChart } from '../components/Chart/BarChart';
import { formatMonto, formatMes } from '../lib/format';
import { DollarSign, Clock, Activity, Users } from 'lucide-react';

export default function Dashboard() {
  const kpis = useApi(() => api.dashboard.kpis());
  const ingresos = useApi(() => api.dashboard.ingresosMensuales());
  const estados = useApi(() => api.dashboard.tratamientosEstado());
  const topMaterials = useApi(() => api.dashboard.topMateriales());

  return (
    <div>
      <div className="view-header">
        <h1 className="view-title">Dashboard</h1>
        <p className="view-subtitle">Resumen general de la clinica</p>
      </div>

      <div className="kpi-grid">
        <KpiCard label="Ingresos del Mes" value={kpis.loading ? '...' : formatMonto(kpis.data?.ingresosMes ?? 0)} icon={DollarSign} />
        <KpiCard label="Ingresos Semana" value={kpis.loading ? '...' : formatMonto(kpis.data?.ingresosSemana ?? 0)} icon={Clock} />
        <KpiCard label="Tratamientos en Curso" value={kpis.loading ? '...' : kpis.data?.tratamientosCurso ?? 0} icon={Activity} variant="warning" />
        <KpiCard label="Docentes Hoy" value={kpis.loading ? '...' : kpis.data?.docentesHoy ?? 0} icon={Users} variant="success" />
      </div>

      <div className="chart-grid">
        <LineChart
          title="Ingresos Mensuales"
          data={(ingresos.data ?? []).map((i) => ({ label: formatMes(i.mes), valor: i.monto }))}
        />
        <BarChart
          title="Top Materiales"
          data={(topMaterials.data ?? []).map((m) => ({ label: m.nombre, valor: m.cantidad }))}
        />
      </div>

      <div className="chart-grid chart-grid-3 mt-4">
        <DonutChart
          title="Estado de Tratamientos"
          data={(estados.data ?? []).map((e) => ({ label: e.estado, valor: e.count }))}
        />
      </div>
    </div>
  );
}
