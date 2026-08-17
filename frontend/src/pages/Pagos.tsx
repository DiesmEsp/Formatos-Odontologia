import { useState } from 'react';
import { X, CircleDollarSign, Eye, DollarSign, Pencil, Trash2 } from 'lucide-react';
import { useApi } from '../hooks/useApi';
import { useToast } from '../hooks/useToast';
import { api } from '../api';
import { CatalogoTabla, type Column } from '../components/CatalogoTabla';
import { Badge } from '../components/Badge';
import { RegistrarPagoModal } from '../components/RegistrarPagoModal';
import { formatMonto, nombreCompleto } from '../lib/format';
import type { Tratamiento, Pago } from '../api/types';

const saldoDe = (t: Tratamiento) => t.monto - t.montoPagado;

export default function Pagos() {
  const activos = useApi(() => api.tratamientos.activos());
  const cerrados = useApi(() => api.tratamientos.cerrados());
  const conSaldo = useApi(() => api.tratamientos.cerradosPorPagar());
  const operadores = useApi(() => api.catalogos.operadores.listar());
  const pacientes = useApi(() => api.catalogos.pacientes.listar());
  const { addToast } = useToast();

  const [pagoTarget, setPagoTarget] = useState<Tratamiento | null>(null);
  const [verTarget, setVerTarget] = useState<Tratamiento | null>(null);
  const [filtroEstado, setFiltroEstado] = useState('');

  const list = [...(activos.data ?? []), ...(cerrados.data ?? [])];
  const operadorMap = new Map((operadores.data ?? []).map((o) => [o.operadorID, nombreCompleto(o.nombres, o.apellidos)]));
  const pacienteMap = new Map((pacientes.data ?? []).map((p) => [p.pacienteID, nombreCompleto(p.nombres, p.apellidos)]));

  const filtrado = filtroEstado ? list.filter((t) => t.estadoPago === filtroEstado) : list;
  const totalPendiente = list.reduce((s, t) => s + Math.max(0, saldoDe(t)), 0);
  const totalPagado = list.reduce((s, t) => s + t.montoPagado, 0);
  const conSaldoCount = (conSaldo.data ?? []).length;

  const columns: Column<Tratamiento>[] = [
    { key: 'id', header: 'ID', width: 50, render: (r) => <span className="num">{r.tratamientoID}</span>, sortValue: (r) => r.tratamientoID },
    { key: 'nombre', header: 'Tratamiento', render: (r) => r.nombreTratamiento, sortValue: (r) => r.nombreTratamiento },
    { key: 'operador', header: 'Operador', render: (r) => operadorMap.get(r.operadorID) ?? `#${r.operadorID}` },
    { key: 'paciente', header: 'Paciente', render: (r) => pacienteMap.get(r.pacienteID) ?? `#${r.pacienteID}` },
    { key: 'fecha', header: 'Fecha', width: 100, render: (r) => r.fecha, sortValue: (r) => r.fecha },
    { key: 'monto', header: 'Monto', width: 100, render: (r) => formatMonto(r.monto), sortValue: (r) => r.monto },
    { key: 'pagado', header: 'Pagado', width: 100, render: (r) => formatMonto(r.montoPagado), sortValue: (r) => r.montoPagado },
    {
      key: 'saldo', header: 'Saldo', width: 100, sortValue: (r) => saldoDe(r),
      render: (r) => <span className={`num ${saldoDe(r) > 0 ? 'text-danger' : 'text-success'}`}>{formatMonto(saldoDe(r))}</span>,
    },
    {
      key: 'estado', header: 'Pago', width: 90,
      render: (r) => <Badge variant={r.estadoPago === 'PAGADO' ? 'success' : r.estadoPago === 'PARCIAL' ? 'warning' : 'neutral'}>{r.estadoPago}</Badge>,
    },
    {
      key: 'acciones', header: '', width: 110, className: 'text-center',
      render: (r) => (
        <div className="flex gap-4 justify-center">
          <button type="button" className="btn btn-ghost btn-sm" title="Registrar pago" onClick={() => setPagoTarget(r)}><DollarSign size={14} /></button>
          <button type="button" className="btn btn-ghost btn-sm" title="Ver pagos" onClick={() => setVerTarget(r)}><Eye size={14} /></button>
        </div>
      ),
    },
  ];

  return (
    <div>
      <div className="view-header">
        <div>
          <h1 className="view-title">Pagos</h1>
          <p className="view-subtitle">Revisión y gestión de pagos de tratamientos</p>
        </div>
      </div>

      {conSaldoCount > 0 && (
        <div className="alert-banner alert-warning mb-16">
          <CircleDollarSign size={16} />
          <span>{conSaldoCount} tratamiento(s) cerrado(s) con saldo pendiente.</span>
        </div>
      )}

      <div className="kpi-grid">
        <div className="kpi-card">
          <span className="kpi-label">Total pendiente</span>
          <span className="kpi-value">{formatMonto(totalPendiente)}</span>
        </div>
        <div className="kpi-card">
          <span className="kpi-label">Total pagado</span>
          <span className="kpi-value">{formatMonto(totalPagado)}</span>
        </div>
        <div className="kpi-card">
          <span className="kpi-label">Con saldo</span>
          <span className="kpi-value">{conSaldoCount}</span>
        </div>
      </div>

      <div className="card">
        <h3 className="card-title mb-12">Todos los tratamientos</h3>
        <CatalogoTabla
          columns={columns}
          data={filtrado}
          loading={activos.loading || cerrados.loading}
          searchEnabled={false}
          emptyTitle="Sin tratamientos"
          emptyText="No hay tratamientos registrados."
          rowKey={(r) => r.tratamientoID}
          filterBar={
            <select className="combo-box" value={filtroEstado} onChange={(e) => setFiltroEstado(e.target.value)}>
              <option value="">Todos los estados</option>
              <option value="PENDIENTE">Pendiente</option>
              <option value="PARCIAL">Parcial</option>
              <option value="PAGADO">Pagado</option>
            </select>
          }
        />
      </div>

      {pagoTarget && (
        <RegistrarPagoModal
          tratamiento={pagoTarget}
          onClose={() => setPagoTarget(null)}
          onSuccess={() => { setPagoTarget(null); activos.refetch(); cerrados.refetch(); conSaldo.refetch(); }}
          addToast={addToast}
        />
      )}
      {verTarget && (
        <VerPagosModal
          tratamiento={verTarget}
          onClose={() => setVerTarget(null)}
          onChanged={() => { activos.refetch(); cerrados.refetch(); conSaldo.refetch(); }}
          addToast={addToast}
        />
      )}
    </div>
  );
}

function VerPagosModal({
  tratamiento, onClose, onChanged, addToast,
}: { tratamiento: Tratamiento; onClose: () => void; onChanged: () => void; addToast: ReturnType<typeof useToast>['addToast'] }) {
  const pagos = useApi(() => api.tratamientos.pagos(tratamiento.tratamientoID), [tratamiento.tratamientoID]);
  const [editar, setEditar] = useState<Pago | null>(null);
  const [montoStr, setMontoStr] = useState('');
  const [fecha, setFecha] = useState('');
  const [saving, setSaving] = useState(false);

  const openEditar = (p: Pago) => { setEditar(p); setMontoStr(String(p.monto)); setFecha(p.fecha); };

  const handleEditar = async () => {
    if (!editar) return;
    setSaving(true);
    try {
      await api.tratamientos.editarPago(editar.pagoID, Number(montoStr), fecha);
      addToast('success', 'Pago actualizado');
      setEditar(null);
      pagos.refetch();
      onChanged();
    } catch (err) { addToast('error', err instanceof Error ? err.message : 'Error al editar pago'); }
    finally { setSaving(false); }
  };

  const handleEliminar = async (p: Pago) => {
    try {
      await api.tratamientos.eliminarPago(p.pagoID);
      addToast('success', 'Pago eliminado');
      pagos.refetch();
      onChanged();
    } catch (err) { addToast('error', err instanceof Error ? err.message : 'Error al eliminar pago'); }
  };

  return (
    <div className="dialog-overlay" onClick={onClose}>
      <div className="dialog-pane mw-460" role="dialog" aria-modal="true" aria-labelledby="ver-pagos-title" onClick={(e) => e.stopPropagation()}>
        <div className="dialog-header">
          <h3 className="dialog-title" id="ver-pagos-title">Pagos de #{tratamiento.tratamientoID}</h3>
          <button className="btn btn-ghost btn-sm" onClick={onClose}><X size={18} /></button>
        </div>
        <div className="dialog-body">
          {(pagos.data ?? []).length === 0 ? (
            <span className="mat-empty">Sin pagos registrados</span>
          ) : (
            <ul className="material-list">
              {(pagos.data ?? []).map((p) => (
                <li key={p.pagoID} className="material-list-item">
                  <span className="material-list-name">{p.fecha}</span>
                  <span className="material-list-cant">{formatMonto(p.monto)}</span>
                  <button type="button" className="btn btn-ghost btn-sm" title="Editar" onClick={() => openEditar(p)}><Pencil size={14} /></button>
                  <button type="button" className="btn btn-ghost btn-sm" title="Eliminar" onClick={() => handleEliminar(p)}><Trash2 size={14} /></button>
                </li>
              ))}
            </ul>
          )}
          {editar && (
            <div className="mt-16">
              <div className="form-group"><label className="form-label">Monto</label><input type="number" className="text-field w-full" value={montoStr} onChange={(e) => setMontoStr(e.target.value)} min={0.01} step="0.01" /></div>
              <div className="form-group"><label className="form-label">Fecha</label><input type="date" className="text-field w-full" value={fecha} onChange={(e) => setFecha(e.target.value)} /></div>
              <div className="flex gap-8">
                <button className="btn btn-primary" onClick={handleEditar} disabled={saving}>{saving ? 'Guardando...' : 'Guardar'}</button>
                <button className="btn btn-secondary" onClick={() => setEditar(null)}>Cancelar</button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
