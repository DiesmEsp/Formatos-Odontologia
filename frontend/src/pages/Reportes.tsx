import { useState } from 'react';
import { FileSpreadsheet, DollarSign, Users, GraduationCap, Calendar, FolderOpen, Clock, BarChart3, ClipboardList } from 'lucide-react';
import { useApi } from '../hooks/useApi';
import { useToast } from '../hooks/useToast';
import { api } from '../api';
import { MonthYearPicker } from '../components/MonthYearPicker';
import { MonthYearRangePicker } from '../components/MonthYearRangePicker';
import { mesActual, anioActual } from '../lib/format';

export default function Reportes() {
  const [mes, setMes] = useState(mesActual());
  const [anio, setAnio] = useState(anioActual());
  const [generating, setGenerating] = useState<string | null>(null);
  const [rangoMesInicio, setRangoMesInicio] = useState(mesActual());
  const [rangoAnioInicio, setRangoAnioInicio] = useState(anioActual());
  const [rangoMesFin, setRangoMesFin] = useState(mesActual());
  const [rangoAnioFin, setRangoAnioFin] = useState(anioActual());
  const { addToast } = useToast();

  const recientes = useApi(() => api.reportes.listarRecientes());
  const operadores = useApi(() => api.catalogos.operadores.listar());
  const [filtroOperador, setFiltroOperador] = useState<number | ''>('');
  const [filtroTipo, setFiltroTipo] = useState('');

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

  const validarRango = (): boolean => {
    if (rangoAnioInicio > rangoAnioFin) return false;
    if (rangoAnioInicio === rangoAnioFin && rangoMesInicio > rangoMesFin) return false;
    return true;
  };

  const generarConsolidado = async () => {
    if (!validarRango()) {
      addToast('error', 'El rango de fechas no es válido. El inicio debe ser anterior al fin.');
      return;
    }
    setGenerating('consolidado');
    try {
      const result = await api.reportes.generarConsolidado(rangoAnioInicio, rangoMesInicio, rangoMesFin);
      addToast('success', `Reporte consolidado generado: ${result.path.split(/[/\\]/).pop()}`);
      recientes.refetch();
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al generar reporte consolidado');
    } finally {
      setGenerating(null);
    }
  };

  const abrirUbicacion = async (filePath: string) => {
    try {
      if (typeof window !== 'undefined' && window.api?.shell?.openPath) {
        const result = await window.api.shell.openPath(filePath);
        if (result.success) {
          addToast('success', 'Ubicacion abierta en el explorador');
        } else {
          addToast('error', result.error || 'No se pudo abrir la ubicacion');
        }
      } else {
        addToast('info', `Archivo: ${filePath.split(/[/\\]/).pop()}\nRuta: ${filePath}`);
      }
    } catch {
      addToast('info', `Archivo: ${filePath.split(/[/\\]/).pop()}\nRuta: ${filePath}`);
    }
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
    {
      tipo: 'tratamiento',
      icon: ClipboardList,
      title: 'Consumo por Tratamiento',
      desc: 'Materiales consumidos por operador y tratamiento cerrado.',
      generar: () => api.reportes.generarTratamiento(anio, mes, filtroOperador || null, filtroTipo || undefined),
    },
    {
      tipo: 'asistencia',
      icon: Clock,
      title: 'Asistencia Docente',
      desc: 'Horas de presencia, ausencias y periodos de salida por docente.',
      generar: () => api.reportes.generarAsistencia(anio, mes),
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
          showButton={false}
        />
      </div>

      <div className="card" style={{ marginBottom: 20 }}>
        <h3 className="card-title" style={{ marginBottom: 14 }}>Reportes mensuales</h3>
        <div className="filter-bar" style={{ display: 'flex', gap: 10, alignItems: 'center', flexWrap: 'wrap', marginBottom: 14 }}>
          <select
            className="combo-box"
            value={filtroOperador}
            onChange={(e) => setFiltroOperador(e.target.value === '' ? '' : Number(e.target.value))}
          >
            <option value="">Todos los operadores</option>
            {(operadores.data ?? []).map((o) => (
              <option key={o.operadorID} value={o.operadorID}>{o.nombres} {o.apellidos}</option>
            ))}
          </select>
          <select className="combo-box" value={filtroTipo} onChange={(e) => setFiltroTipo(e.target.value)}>
            <option value="">Todos los tipos</option>
            <option value="NORMAL">Normal</option>
            <option value="CONTINUO">Continuo</option>
          </select>
          <span className="text-muted text-sm">Filtros de "Consumo por Tratamiento"</span>
        </div>
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

      <div className="card" style={{ marginBottom: 20 }}>
        <h3 className="card-title" style={{ marginBottom: 14 }}>Reporte consolidado por período</h3>
        <p className="text-muted text-sm" style={{ marginBottom: 14 }}>
          Genere un reporte unificado (materiales, ingresos y asistencia) para un rango personalizado de meses.
        </p>
        <MonthYearRangePicker
          mesInicio={rangoMesInicio}
          anioInicio={rangoAnioInicio}
          mesFin={rangoMesFin}
          anioFin={rangoAnioFin}
          onMesInicioChange={setRangoMesInicio}
          onAnioInicioChange={setRangoAnioInicio}
          onMesFinChange={setRangoMesFin}
          onAnioFinChange={setRangoAnioFin}
          onGenerate={generarConsolidado}
          generating={generating === 'consolidado'}
        />
        {!validarRango() && (
          <div className="alert-banner alert-warning" style={{ marginTop: 12 }}>
            <BarChart3 size={16} />
            <span>La fecha de inicio debe ser anterior o igual a la fecha de fin.</span>
          </div>
        )}
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
                    <button className="btn btn-ghost btn-sm" onClick={() =>                       abrirUbicacion(r.path)}>
                      <FolderOpen size={14} /> Abrir ubicacion
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
