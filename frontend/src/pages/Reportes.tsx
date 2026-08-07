import { useState } from 'react';
import { FileSpreadsheet, DollarSign, Users, GraduationCap, Calendar, FolderOpen } from 'lucide-react';
import { useApi } from '../hooks/useApi';
import { useToast } from '../hooks/useToast';
import { api } from '../api';
import { MonthYearPicker } from '../components/MonthYearPicker';
import { mesActual, anioActual } from '../lib/format';

export default function Reportes() {
  const [mes, setMes] = useState(mesActual());
  const [anio, setAnio] = useState(anioActual());
  const [generating, setGenerating] = useState<string | null>(null);
  const { addToast } = useToast();

  const recientes = useApi(() => api.reportes.listarRecientes());

  const generar = async (tipo: string, fn: () => Promise<any>) => {
    setGenerating(tipo);
    try {
      const result = await fn();
      const path = result.path || result.reporteMateriales;
      addToast('success', `Reporte generado: ${path.split(/[/\\]/).pop()}`);
      recientes.refetch();
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al generar reporte');
    } finally {
      setGenerating(null);
    }
  };

  const abrirArchivo = (path: string) => {
    if (window.api) {
      window.api.health.check();
    }
    window.open(`file://${path}`, '_blank');
  };

  const reportes = [
    {
      tipo: 'materiales',
      icon: FileSpreadsheet,
      title: 'Materiales Generales',
      desc: 'Consolidado de materiales consumidos en el mes, convertidos a unidad base.',
      generar: () => api.reportes.generarMateriales(anio, mes),
    },
    {
      tipo: 'economico',
      icon: DollarSign,
      title: 'Ingresos Financieros',
      desc: 'Montos facturados por grado y tipo de especialista, con total, pagado y pendiente.',
      generar: () => api.reportes.generarEconomico(anio, mes),
    },
    {
      tipo: 'docente',
      icon: Users,
      title: 'Consumo Docente',
      desc: 'Materiales entregados por docente (consolidado + detalle diario).',
      generar: () => api.reportes.generarDocente(anio, mes),
    },
    {
      tipo: 'especialista',
      icon: GraduationCap,
      title: 'Consumo Especialista',
      desc: 'Materiales consumidos por especialista en tratamientos cerrados.',
      generar: () => api.reportes.generarEspecialista(anio, mes),
    },
  ];

  return (
    <div>
      <div className="view-header">
        <h1 className="view-title">Reportes</h1>
        <p className="view-subtitle">Exportacion de reportes mensuales y anuales a Excel</p>
      </div>

      <div className="card" style={{ marginBottom: 20 }}>
        <h3 className="card-title" style={{ marginBottom: 14 }}>Periodo del reporte</h3>
        <MonthYearPicker
          mes={mes}
          anio={anio}
          onMesChange={setMes}
          onAnioChange={setAnio}
          onGenerate={() => {}}
          generating={false}
          label=""
        />
      </div>

      <div className="card" style={{ marginBottom: 20 }}>
        <h3 className="card-title" style={{ marginBottom: 14 }}>Reportes mensuales</h3>
        <div className="report-grid">
          {reportes.map((r) => (
            <div key={r.tipo} className="report-card">
              <div className="report-icon">
                <r.icon size={18} />
              </div>
              <div className="report-info">
                <span className="report-title">{r.title}</span>
                <span className="report-desc">{r.desc}</span>
              </div>
              <button
                className="btn btn-primary btn-sm"
                onClick={() => generar(r.tipo, r.generar)}
                disabled={!!generating}
              >
                {generating === r.tipo ? 'Generando...' : 'Generar Excel'}
              </button>
            </div>
          ))}
        </div>
      </div>

      <div className="card" style={{ marginBottom: 20 }}>
        <h3 className="card-title" style={{ marginBottom: 14 }}>Reporte anual</h3>
        <div className="report-card" style={{ maxWidth: 520 }}>
          <div className="report-icon" style={{ backgroundColor: 'var(--color-success-bg)' }}>
            <Calendar size={18} color="var(--color-success-text)" />
          </div>
          <div className="report-info">
            <span className="report-title">Consolidado Anual {anio}</span>
            <span className="report-desc">Los 4 reportes anteriores desglosados mes por mes para el año seleccionado.</span>
          </div>
          <button
            className="btn btn-success btn-sm"
            onClick={() => generar('anual', () => api.reportes.generarAnual(anio))}
            disabled={!!generating}
          >
            {generating === 'anual' ? 'Generando...' : `Generar Reporte Anual ${anio}`}
          </button>
        </div>
      </div>

      <div className="card">
        <h3 className="card-title" style={{ marginBottom: 14 }}>Reportes generados recientemente</h3>
        {(recientes.data ?? []).length === 0 ? (
          <div className="empty-state">
            <span className="empty-text">No hay reportes generados aun.</span>
          </div>
        ) : (
          <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Archivo</th>
                <th style={{ width: 100 }}>Tamaño</th>
                <th style={{ width: 80 }} />
              </tr>
            </thead>
            <tbody>
              {(recientes.data ?? []).map((r, i) => (
                <tr key={i}>
                  <td className="num" style={{ fontFamily: 'var(--font-mono)', fontSize: 'var(--font-md)' }}>{r.nombre}</td>
                  <td className="num">{r.tamano} B</td>
                  <td>
                    <button className="btn btn-ghost btn-sm" onClick={() => abrirArchivo(r.path)}>
                      <FolderOpen size={14} /> Abrir
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          </div>
        )}
      </div>
    </div>
  );
}
