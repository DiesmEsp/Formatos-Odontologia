import { Link } from 'react-router-dom';
import { useApi } from '../hooks/useApi';
import { api } from '../api';
import { KpiCard } from '../components/KpiCard';
import { LineChart } from '../components/Chart/LineChart';
import { formatMonto, formatMes, formatearHora } from '../lib/format';
import { nombreClinicaSesion } from '../lib/clinicaStore';
import { DollarSign, Clock, Activity, Users, Archive, CalendarCheck, FileSpreadsheet, Stethoscope, Monitor } from 'lucide-react';
import { COLORS_CHART } from '../lib/constants';

function obtenerSaludo(): string {
  const hora = new Date().getHours();
  if (hora < 12) return 'Buenos días';
  if (hora < 19) return 'Buenas tardes';
  return 'Buenas noches';
}

function fechaDeHoy(): string {
  const fecha = new Date().toLocaleDateString('es-PE', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  });
  return fecha.charAt(0).toUpperCase() + fecha.slice(1);
}

export default function Dashboard() {
  const kpis = useApi(() => api.dashboard.kpis());
  const ingresos = useApi(() => api.dashboard.ingresosMensuales());
  const estados = useApi(() => api.dashboard.tratamientosEstado());
  const topMaterials = useApi(() => api.dashboard.topMateriales());
  const asistenciaHoy = useApi(() => api.dashboard.asistenciaHoy());

  const totalTratamientos = (estados.data ?? []).reduce((sum, e) => sum + e.count, 0);

  const errors = [kpis.error, ingresos.error, estados.error, topMaterials.error, asistenciaHoy.error].filter(Boolean);

  return (
    <div>
      <div className="view-header">
        <div>
          <h1 className="view-title">{obtenerSaludo()}</h1>
          <p className="subtitle">{fechaDeHoy()} · {nombreClinicaSesion() ?? 'Clinica Odontologica UNMSM'}</p>
        </div>
      </div>

      {errors.length > 0 && (
        <div className="alert-banner alert-warning" style={{ marginBottom: 16 }}>
          <span>No se pudieron cargar algunos datos. {errors[0]}</span>
        </div>
      )}

      <div className="kpi-grid">
        <KpiCard label="Ingresos del Mes" value={kpis.loading ? '...' : formatMonto(kpis.data?.ingresosMes ?? 0)} icon={DollarSign} />
        <KpiCard label="Ingresos Semana" value={kpis.loading ? '...' : formatMonto(kpis.data?.ingresosSemana ?? 0)} icon={Clock} />
        <KpiCard label="Tratamientos en Curso" value={kpis.loading ? '...' : kpis.data?.tratamientosCurso ?? 0} icon={Activity} variant="warning" />
        <KpiCard label="Docentes Hoy" value={kpis.loading ? '...' : kpis.data?.docentesHoy ?? 0} icon={Users} variant="success" />
      </div>

      <div className="chart-grid" style={{ marginTop: 20 }}>
        <LineChart
          title="Ingresos mensuales"
          data={(ingresos.data ?? []).map((i) => ({ label: formatMes(i.mes).substring(0, 3), valor: i.monto }))}
        />

        <div className="chart-card">
          <div className="chart-head">
            <div>
              <span className="chart-title">Tratamientos por estado</span>
            </div>
          </div>
          <div style={{ position: 'relative', height: 180 }}>
            <svg viewBox="0 0 120 120" style={{ width: '100%', height: '100%' }} role="img">
              <g transform="rotate(-90 60 60)">
                {(estados.data ?? []).map((e, idx) => {
                  const total = (estados.data ?? []).reduce((s, ee) => s + ee.count, 0);
                  const pct = total > 0 ? (e.count / total) * 251.3 : 0;
                  let offset = 0;
                  for (let i = 0; i < idx; i++) {
                    offset += total > 0 ? (estados.data![i].count / total) * 251.3 : 0;
                  }
                  return (
                    <circle
                      key={e.estado}
                      cx="60" cy="60" r="40"
                      fill="none"
                      stroke={COLORS_CHART[idx % COLORS_CHART.length]}
                      strokeWidth="15"
                      strokeDasharray={`${pct} 251.3`}
                      strokeDashoffset={-offset}
                      strokeLinecap="butt"
                    />
                  );
                })}
              </g>
              <text x="60" y="56" textAnchor="middle" fontFamily="var(--font-mono)" fontSize="18" fontWeight="600" fill="var(--color-text)">{totalTratamientos}</text>
              <text x="60" y="70" textAnchor="middle" fontSize="9" fill="var(--color-text-muted)">del mes</text>
            </svg>
          </div>
          <div className="legend">
            {(estados.data ?? []).map((e, idx) => (
              <div key={e.estado} className="legend-item">
                <span className="legend-swatch" style={{ background: COLORS_CHART[idx % COLORS_CHART.length] }} />
                <span>{e.estado} {e.count}</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="chart-grid mt-4" style={{ marginTop: 14 }}>
        <div className="chart-card">
          <div className="chart-head">
            <div>
              <span className="chart-title">Materiales mas utilizados</span>
            </div>
            <span className="chart-meta">top 5</span>
          </div>
          <div className="chart-body">
            {(topMaterials.data ?? []).map((m, idx) => {
              const max = Math.max(...(topMaterials.data ?? []).map((x) => x.cantidad), 1);
              const pct = (m.cantidad / max) * 100;
              return (
                <div key={idx} className="hbar">
                  <span className="hbar-label">{m.nombre}</span>
                  <div className="hbar-track">
                    <div className="hbar-fill" style={{ width: `${pct}%`, background: COLORS_CHART[idx % COLORS_CHART.length] }} />
                  </div>
                  <span className="hbar-val">{m.cantidad}</span>
                </div>
              );
            })}
            {(topMaterials.data ?? []).length === 0 && (
              <span className="text-muted text-sm">Sin datos</span>
            )}
          </div>
        </div>

        <div className="chart-card">
          <div className="chart-head">
            <span className="chart-title">Asistencia docente hoy</span>
          </div>
          <div className="att-list">
            {(asistenciaHoy.data ?? []).map((a) => {
              let estadoLabel: string;
              let ledClass: string;
              if (!a.presente) {
                estadoLabel = 'Ausente';
                ledClass = 'led-danger';
              } else if (a.horaSalida) {
                estadoLabel = `Finalizo ${formatearHora(a.horaSalida)}`;
                ledClass = '';
              } else if (a.enAusencia) {
                estadoLabel = 'Ausente temp.';
                ledClass = 'led-warning';
              } else {
                estadoLabel = a.horaEntrada ? `Desde ${formatearHora(a.horaEntrada)}` : 'Presente';
                ledClass = 'led-ok';
              }

              return (
                <div key={a.docenteID} className="att-row">
                  <span className={`att-avatar ${!a.presente ? 'att-avatar-ausente' : a.enAusencia ? '' : ''}`}>
                    {a.nombres.charAt(0)}{a.apellidos.charAt(0)}
                  </span>
                  <div className="att-name">{a.nombres} {a.apellidos}</div>
                  <div className="att-flag">
                    <span className={`led ${ledClass}`} />
                    <span>{estadoLabel}</span>
                  </div>
                </div>
              );
            })}
            {asistenciaHoy.loading && <span className="text-muted text-sm">Cargando...</span>}
          </div>
        </div>
      </div>

      <h3 style={{ fontSize: '0.95rem', fontWeight: 600, marginTop: 28, marginBottom: 12, color: 'var(--color-text)' }}>Acceso rapido</h3>
      <div className="grid-cards">
        <Link to="/tratamientos" className="dash-card">
          <div className="dash-icon"><Stethoscope size={20} /></div>
          <h4 className="dash-card-title">Tratamientos</h4>
          <p className="dash-card-desc">Registro de atención con materiales predefinidos y adicionales</p>
        </Link>
        <Link to="/asistencia" className="dash-card">
          <div className="dash-icon"><CalendarCheck size={20} /></div>
          <h4 className="dash-card-title">Asistencia Docente</h4>
          <p className="dash-card-desc">Control diario de entrega de materiales a docentes</p>
        </Link>
        <Link to="/catalogos" className="dash-card">
          <div className="dash-icon"><Archive size={20} /></div>
          <h4 className="dash-card-title">Catalogos</h4>
          <p className="dash-card-desc">Materiales, docentes, especialistas y tratamientos predefinidos</p>
        </Link>
        <Link to="/reportes" className="dash-card">
          <div className="dash-icon"><FileSpreadsheet size={20} /></div>
          <h4 className="dash-card-title">Reportes</h4>
          <p className="dash-card-desc">Exportacion Excel de materiales, ingresos, docentes y especialistas</p>
        </Link>
        <Link to="/unidades" className="dash-card">
          <div className="dash-icon"><Monitor size={20} /></div>
          <h4 className="dash-card-title">Unidades</h4>
          <p className="dash-card-desc">Gestión de unidades de atención de la clínica</p>
        </Link>
      </div>
    </div>
  );
}
