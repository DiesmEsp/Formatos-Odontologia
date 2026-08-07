import { useState, useEffect, useRef } from 'react';
import { useApi } from '../hooks/useApi';
import { useToast } from '../hooks/useToast';
import { api } from '../api';
import { StationCard } from '../components/StationCard';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { Badge } from '../components/Badge';
import { SearchableCombo, type SearchableOption } from '../components/SearchableCombo';
import { MaterialTable, type MaterialRow } from '../components/MaterialTable';
import { X, DollarSign, RotateCcw, AlertTriangle, ArrowLeftRight } from 'lucide-react';
import { formatMonto, hoyISO, nombreCompleto } from '../lib/format';
import type { Tratamiento, Unidad } from '../api/types';

export default function Tratamientos() {
  const unidades = useApi(() => api.unidades.listar());
  const tratamientos = useApi(() => api.tratamientos.activos());
  const { addToast } = useToast();

  const [crearUnidad, setCrearUnidad] = useState<Unidad | null>(null);
  const [detalleTratamiento, setDetalleTratamiento] = useState<Tratamiento | null>(null);

  const tratamientosActivos = tratamientos.data ?? [];
  const unidadesList = unidades.data ?? [];

  const getTratamientoEnUnidad = (unidadId: number) => {
    return tratamientosActivos.find((t) => t.unidadID === unidadId && t.estado !== 'CERRADO' && t.estado !== 'ANULADO') ?? null;
  };

  return (
    <div>
      <div className="view-header">
        <div>
          <h1 className="view-title">Tratamientos en Curso</h1>
          <p className="subtitle">Seleccione una unidad libre para iniciar un nuevo tratamiento</p>
        </div>
      </div>

      <div className="station-grid">
        {unidadesList.map((u) => (
          <StationCard
            key={u.unidadID}
            unidadNro={u.unidadNro}
            tratamiento={getTratamientoEnUnidad(u.unidadID)}
            onClick={() => {
              const t = getTratamientoEnUnidad(u.unidadID);
              if (t) {
                setDetalleTratamiento(t);
              } else {
                setCrearUnidad(u);
              }
            }}
          />
        ))}
        {unidadesList.length === 0 && (
          <div className="empty-state" style={{ gridColumn: '1 / -1' }}>
            <span className="empty-title">No hay unidades registradas</span>
            <span className="empty-text">Cree unidades en la seccion de Gestion para comenzar.</span>
          </div>
        )}
      </div>

      {crearUnidad && (
        <CrearTratamientoModal
          unidad={crearUnidad}
          onClose={() => setCrearUnidad(null)}
          onSuccess={() => {
            setCrearUnidad(null);
            tratamientos.refetch();
          }}
          addToast={addToast}
        />
      )}

      {detalleTratamiento && (
        <DetalleTratamientoSubventana
          tratamiento={detalleTratamiento}
          onClose={() => {
            setDetalleTratamiento(null);
            tratamientos.refetch();
          }}
          addToast={addToast}
        />
      )}
    </div>
  );
}

function CrearTratamientoModal({
  unidad,
  onClose,
  onSuccess,
  addToast,
}: {
  unidad: Unidad;
  onClose: () => void;
  onSuccess: () => void;
  addToast: ReturnType<typeof useToast>['addToast'];
}) {
  const [fecha, setFecha] = useState(hoyISO());
  const [pacienteId, setPacienteId] = useState<number | null>(null);
  const [operadorId, setOperadorId] = useState<number | null>(null);
  const [tratPredId, setTratPredId] = useState<number | null>(null);
  const [monto, setMonto] = useState<number>(0);
  const [tipo, setTipo] = useState<string>('NORMAL');
  const [saving, setSaving] = useState(false);
  const [qPac, setQPac] = useState('');
  const [qOpe, setQOpe] = useState('');
  const [qTrat, setQTrat] = useState('');

  const pacientes = useApi(() => api.catalogos.pacientes.listar(qPac || undefined), [qPac]);
  const operadores = useApi(() => api.catalogos.operadores.listar(qOpe || undefined), [qOpe]);
  const tratsPred = useApi(() => api.catalogos.tratamientosPred.listar(qTrat || undefined), [qTrat]);

  const pOptions: SearchableOption[] = (pacientes.data ?? []).map((p) => ({ id: p.pacienteID, label: nombreCompleto(p.nombres, p.apellidos) }));
  const oOptions: SearchableOption[] = (operadores.data ?? []).map((o) => ({ id: o.operadorID, label: nombreCompleto(o.nombres, o.apellidos), badge: o.grado }));
  const tOptions: SearchableOption[] = (tratsPred.data ?? []).map((t) => ({ id: t.tratPredID, label: t.nombreTratamiento, extra: t.montoSugerido != null ? `S/ ${t.montoSugerido.toFixed(2)}` : undefined }));

  const handleTratChange = (id: number | null) => {
    setTratPredId(id);
    if (id) {
      const tp = tratsPred.data?.find((t) => t.tratPredID === id);
      if (tp?.montoSugerido != null && tipo !== 'CONTINUO') {
        setMonto(tp.montoSugerido);
      }
    }
  };

  const handleTipoChange = (nuevoTipo: string) => {
    setTipo(nuevoTipo);
    if (nuevoTipo === 'CONTINUO') {
      setMonto(0);
    }
  };

  const handleGuardar = async () => {
    if (!operadorId || !pacienteId) {
      addToast('error', 'Seleccione paciente y especialista');
      return;
    }
    setSaving(true);
    try {
      await api.tratamientos.crear({
        operadorID: operadorId,
        pacienteID: pacienteId,
        unidadID: unidad.unidadID,
        fecha,
        tratPredID: tratPredId,
        monto: tipo === 'CONTINUO' ? null : monto,
        tipo,
      });
      addToast('success', 'Tratamiento creado correctamente');
      onSuccess();
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al crear tratamiento');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="dialog-overlay" onClick={onClose}>
      <div className="dialog-pane" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 560 }}>
        <div className="dialog-header">
          <h3 className="dialog-title">Crear Tratamiento</h3>
          <button className="btn btn-ghost btn-sm" onClick={onClose}><X size={18} /></button>
        </div>
        <div className="dialog-body">
          <div className="form-group">
            <label className="form-label">Unidad</label>
            <input className="text-field" value={`Unidad ${unidad.unidadNro}`} readOnly style={{ width: '100%' }} />
          </div>
          <div className="form-group">
            <label className="form-label">Fecha</label>
            <input type="date" className="text-field" value={fecha} onChange={(e) => setFecha(e.target.value)} style={{ width: '100%' }} />
          </div>
          <div className="form-group">
            <label className="form-label">Paciente</label>
            <SearchableCombo options={pOptions} value={pacienteId} onChange={setPacienteId} onSearch={setQPac} placeholder="Buscar paciente..." allowCreate />
          </div>
          <div className="form-group">
            <label className="form-label">Especialista</label>
            <SearchableCombo options={oOptions} value={operadorId} onChange={setOperadorId} onSearch={setQOpe} placeholder="Buscar especialista..." allowCreate />
          </div>
          <div className="form-group">
            <label className="form-label">Tipo de tratamiento</label>
            <SearchableCombo options={tOptions} value={tratPredId} onChange={handleTratChange} onSearch={setQTrat} placeholder="Buscar tratamiento..." allowCreate />
          </div>
          <div className="form-group">
            <label className="form-label">Monto total</label>
            <input type="number" className="text-field" value={monto} onChange={(e) => setMonto(Number(e.target.value))} step="0.01" min={0} disabled={tipo === 'CONTINUO'} style={{ width: '100%' }} />
          </div>
          <div className="form-group">
            <label className="form-label">Tipo</label>
            <select className="combo-box" value={tipo} onChange={(e) => handleTipoChange(e.target.value)} style={{ width: '100%' }}>
              <option value="NORMAL">Normal</option>
              <option value="CONTINUO">Continuo</option>
            </select>
          </div>
          {tipo === 'CONTINUO' && (
            <div className="alert-banner alert-info">
              Los tratamientos continuos no tienen monto fijo. El estado de pago se establece automaticamente como PAGADO.
            </div>
          )}
        </div>
        <div className="dialog-footer">
          <button className="btn btn-secondary" onClick={onClose}>Cancelar</button>
          <button className="btn btn-primary" onClick={handleGuardar} disabled={saving}>
            {saving ? 'Guardando...' : 'Crear Tratamiento'}
          </button>
        </div>
      </div>
    </div>
  );
}

function DetalleTratamientoSubventana({
  tratamiento: initialTrat,
  onClose,
  addToast,
}: {
  tratamiento: Tratamiento;
  onClose: () => void;
  addToast: ReturnType<typeof useToast>['addToast'];
}) {
  const [tratamiento, setTratamiento] = useState<Tratamiento>(initialTrat);
  const [materialRows, setMaterialRows] = useState<MaterialRow[]>([]);
  const [showPago, setShowPago] = useState(false);
  const [showAnular, setShowAnular] = useState(false);
  const [abono, setAbono] = useState(0);
  const [saving, setSaving] = useState(false);

  const materiales = useApi(() => api.catalogos.materiales.listar());
  const mounted = useRef(true);

  const cargarDatos = async () => {
    try {
      const t = await api.tratamientos.buscarPorId(initialTrat.tratamientoID);
      if (!mounted.current) return;
      setTratamiento(t);
      const mats = await api.tratamientos.materialesConNombre(t.tratamientoID);
      if (!mounted.current) return;
      setMaterialRows(mats.map((m) => ({ key: `mat-${m.materialesListID}`, materialId: m.materialID, nombreMaterial: m.nombreMaterial, cantidad: m.cantidad })));
    } catch {}
  };

  useEffect(() => {
    mounted.current = true;
    cargarDatos();
    return () => { mounted.current = false; };
  }, [initialTrat.tratamientoID]);

  const handleAddRow = () => {
    setMaterialRows((prev) => [...prev, { key: `new-${Date.now()}`, materialId: null, nombreMaterial: '', cantidad: 0 }]);
  };

  const handleRemoveRow = async (key: string) => {
    const row = materialRows.find((r) => r.key === key);
    if (row?.key.startsWith('mat-')) {
      const id = Number(row.key.replace('mat-', ''));
      try {
        await api.tratamientos.quitarMaterial(id);
        addToast('success', 'Material eliminado');
      } catch (err) {
        addToast('error', err instanceof Error ? err.message : 'Error');
        return;
      }
    }
    setMaterialRows((prev) => prev.filter((r) => r.key !== key));
  };

  const handleMaterialChange = async (key: string, materialId: number) => {
    setMaterialRows((prev) => prev.map((r) => r.key === key ? { ...r, materialId } : r));
    if (!key.startsWith('new-')) return;
    try {
      await api.tratamientos.agregarMaterial(tratamiento.tratamientoID, { materialId, cantidad: 0 });
      cargarDatos();
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al agregar material');
    }
  };

  const handleCantidadChange = async (key: string, cantidad: number) => {
    setMaterialRows((prev) => prev.map((r) => r.key === key ? { ...r, cantidad } : r));
    if (!key.startsWith('mat-')) return;
    const id = Number(key.replace('mat-', ''));
    try { await api.tratamientos.actualizarCantidad(id, cantidad); } catch {}
  };

  const handleCerrar = async () => {
    setSaving(true);
    try {
      await api.tratamientos.cerrar(tratamiento.tratamientoID);
      addToast('success', 'Tratamiento cerrado');
      onClose();
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al cerrar');
    } finally { setSaving(false); }
  };

  const handleAnular = async (motivo?: string) => {
    if (!motivo) return;
    try {
      await api.tratamientos.anular(tratamiento.tratamientoID, motivo);
      addToast('success', 'Tratamiento anulado');
      onClose();
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al anular');
    }
    setShowAnular(false);
  };

  const handleReabrir = async () => {
    try {
      await api.tratamientos.reabrir(tratamiento.tratamientoID);
      const t = await api.tratamientos.buscarPorId(tratamiento.tratamientoID);
      setTratamiento(t);
      addToast('success', 'Tratamiento reabierto');
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al reabrir');
    }
  };

  const handleRegistrarPago = async () => {
    if (abono <= 0) return;
    try {
      await api.tratamientos.registrarPago(tratamiento.tratamientoID, abono);
      const t = await api.tratamientos.buscarPorId(tratamiento.tratamientoID);
      setTratamiento(t);
      addToast('success', 'Pago registrado');
      setShowPago(false);
      setAbono(0);
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al registrar pago');
    }
  };

  const handleCambiarTipo = async () => {
    const nuevo = tratamiento.tipo === 'NORMAL' ? 'CONTINUO' : 'NORMAL';
    try {
      await api.tratamientos.cambiarTipo(tratamiento.tratamientoID, nuevo);
      const t = await api.tratamientos.buscarPorId(tratamiento.tratamientoID);
      setTratamiento(t);
      addToast('success', `Tipo cambiado a ${nuevo}`);
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al cambiar tipo');
    }
  };

  const getEstadoVariant = (e: string) => e === 'CERRADO' ? 'success' : e === 'ANULADO' ? 'danger' : 'info';

  const saldo = tratamiento.monto - tratamiento.montoPagado;

  return (
    <>
      <div className="subventana-overlay">
        <div className="subventana">
          <div className="subventana-header">
            <div>
              <h3 className="dialog-title">Detalle de Tratamiento #{tratamiento.tratamientoID}</h3>
            </div>
            <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
              <Badge variant={getEstadoVariant(tratamiento.estado)}>{tratamiento.estado}</Badge>
              <button className="btn btn-ghost btn-sm" onClick={onClose}><X size={18} /></button>
            </div>
          </div>

          <div className="subventana-body">
            <div className="sv-grid">
              <div className="sv-row"><span className="sv-label">Paciente</span><span>#{tratamiento.pacienteID}</span></div>
              <div className="sv-row"><span className="sv-label">Especialista</span><span>#{tratamiento.operadorID}</span></div>
              <div className="sv-row"><span className="sv-label">Fecha</span><span>{tratamiento.fecha}</span></div>
              <div className="sv-row"><span className="sv-label">Monto total</span><span className="num">{formatMonto(tratamiento.monto)}</span></div>
              <div className="sv-row"><span className="sv-label">Monto pagado</span><span className="num">{formatMonto(tratamiento.montoPagado)}</span></div>
              <div className="sv-row"><span className="sv-label">Tipo</span><Badge variant="info">{tratamiento.tipo}</Badge></div>
              <div className="sv-row"><span className="sv-label">Pago</span><Badge variant={tratamiento.estadoPago === 'PAGADO' ? 'success' : tratamiento.estadoPago === 'PARCIAL' ? 'warning' : 'neutral'}>{tratamiento.estadoPago}</Badge></div>
            </div>

            {saldo > 0 && tratamiento.estado === 'ABIERTO' && (
              <div className="alert-banner alert-warning" style={{ marginTop: 16 }}>
                <AlertTriangle size={16} />
                <span>Saldo pendiente: {formatMonto(saldo)}</span>
              </div>
            )}

            <h4 style={{ marginTop: 20, marginBottom: 12 }}>Materiales del tratamiento</h4>
            <MaterialTable
              rows={materialRows}
              materials={materiales.data ?? []}
              onAdd={handleAddRow}
              onRemove={handleRemoveRow}
              onMaterialChange={handleMaterialChange}
              onCantidadChange={handleCantidadChange}
            />
          </div>

          <div className="subventana-footer">
            <span className="text-muted text-sm">{materialRows.length} materiales registrados</span>
            <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
              {tratamiento.estado === 'ABIERTO' && (
                <>
                  <button className="btn btn-success" onClick={handleCerrar} disabled={saving}>Cerrar tratamiento</button>
                  <button className="btn btn-primary" onClick={() => setShowPago(true)}>
                    <DollarSign size={14} /> Registrar pago
                  </button>
                  <button className="btn btn-secondary" onClick={handleCambiarTipo}>
                    <ArrowLeftRight size={14} /> {tratamiento.tipo === 'NORMAL' ? 'CONTINUO' : 'NORMAL'}
                  </button>
                  <button className="btn btn-danger" onClick={() => setShowAnular(true)}>
                    <AlertTriangle size={14} /> Anular
                  </button>
                </>
              )}
              {tratamiento.estado === 'CERRADO' && (
                <>
                  <button className="btn btn-secondary" onClick={handleReabrir}>
                    <RotateCcw size={14} /> Reabrir
                  </button>
                  <button className="btn btn-primary" onClick={() => setShowPago(true)}>
                    <DollarSign size={14} /> Registrar pago
                  </button>
                  <button className="btn btn-danger" onClick={() => setShowAnular(true)}>
                    <AlertTriangle size={14} /> Anular
                  </button>
                </>
              )}
            </div>
          </div>
        </div>
      </div>

      {showPago && (
        <div className="dialog-overlay" onClick={() => setShowPago(false)} style={{ zIndex: 210 }}>
          <div className="dialog-pane" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 420 }}>
            <div className="dialog-header">
              <h3 className="dialog-title">Registrar Pago</h3>
              <button className="btn btn-ghost btn-sm" onClick={() => setShowPago(false)}><X size={18} /></button>
            </div>
            <div className="dialog-body">
              <div className="sv-grid" style={{ marginBottom: 16 }}>
                <div className="sv-row"><span className="sv-label">Monto total</span><span className="num">{formatMonto(tratamiento.monto)}</span></div>
                <div className="sv-row"><span className="sv-label">Pagado</span><span className="num">{formatMonto(tratamiento.montoPagado)}</span></div>
                <div className="sv-row"><span className="sv-label">Saldo</span><span className="num" style={{ color: saldo > 0 ? 'var(--color-danger-text)' : 'var(--color-success-text)' }}>{formatMonto(saldo)}</span></div>
              </div>
              <div className="form-group">
                <label className="form-label">Monto a abonar</label>
                <input type="number" className="text-field" value={abono} onChange={(e) => setAbono(Number(e.target.value))} min={0.01} step="0.01" style={{ width: '100%' }} />
              </div>
            </div>
            <div className="dialog-footer">
              <button className="btn btn-secondary" onClick={() => setShowPago(false)}>Cancelar</button>
              <button className="btn btn-primary" onClick={handleRegistrarPago} disabled={abono <= 0}>Registrar</button>
            </div>
          </div>
        </div>
      )}

      <ConfirmDialog
        open={showAnular}
        title="Anular tratamiento"
        message={`Confirme que desea anular el tratamiento #${tratamiento.tratamientoID}.`}
        confirmLabel="Si, anular"
        variant="danger"
        requireMotivo
        onConfirm={handleAnular}
        onCancel={() => setShowAnular(false)}
      />
    </>
  );
}
