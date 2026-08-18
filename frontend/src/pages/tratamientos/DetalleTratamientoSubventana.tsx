import { useState, useEffect, useRef, useCallback } from 'react';
import { X, DollarSign, RotateCcw, AlertTriangle, ArrowLeftRight, Plus } from 'lucide-react';
import { useApi } from '../../hooks/useApi';
import { useToast } from '../../hooks/useToast';
import { api } from '../../api';
import { ConfirmDialog } from '../../components/ConfirmDialog';
import { Badge } from '../../components/Badge';
import { MaterialTable, type MaterialRow } from '../../components/MaterialTable';
import { RegistrarPagoModal } from '../../components/RegistrarPagoModal';
import { RegistrarAvanceModal } from '../../components/RegistrarAvanceModal';
import { formatMonto } from '../../lib/format';
import type { Tratamiento, Pago, TratamientoAvance, ConsolidadoTratamiento } from '../../api/types';

export function DetalleTratamientoSubventana({
  tratamiento: initialTrat, operadorNombre, pacienteNombre, onClose, addToast,
}: { tratamiento: Tratamiento; operadorNombre?: string; pacienteNombre?: string; onClose: () => void; addToast: ReturnType<typeof useToast>['addToast'] }) {
  const [tratamiento, setTratamiento] = useState<Tratamiento>(initialTrat);
  const [materialRows, setMaterialRows] = useState<MaterialRow[]>([]);
  const [showPago, setShowPago] = useState(false);
  const [showAvance, setShowAvance] = useState(false);
  const [showAnular, setShowAnular] = useState(false);
  const [showAnularAvance, setShowAnularAvance] = useState<TratamientoAvance | null>(null);
  const [saving, setSaving] = useState(false);
  const [savingMat, setSavingMat] = useState(false);
  const [dirty, setDirty] = useState(false);
  const [pagos, setPagos] = useState<Pago[]>([]);
  const [avances, setAvances] = useState<TratamientoAvance[]>([]);
  const [consolidado, setConsolidado] = useState<ConsolidadoTratamiento | null>(null);
  const materiales = useApi(() => api.catalogos.materiales.listar());
  const mounted = useRef(true);
  const originalRowsRef = useRef<MaterialRow[]>([]);
  const materialRowsRef = useRef<MaterialRow[]>([]);

  const cargarDatos = useCallback(async () => {
    try {
      const t = await api.tratamientos.buscarPorId(initialTrat.tratamientoID);
      if (!mounted.current) return;
      setTratamiento(t);
      const mats = await api.tratamientos.materialesConNombre(t.tratamientoID);
      if (!mounted.current) return;
      const rows: MaterialRow[] = mats.map((m) => ({
        key: `mat-${m.materialesListID}`,
        materialId: m.materialID,
        nombreMaterial: m.nombreMaterial,
        cantidad: m.cantidad,
      }));
      setMaterialRows(rows);
      materialRowsRef.current = rows;
      originalRowsRef.current = rows.map((r) => ({ ...r }));
      setDirty(false);

      const pagosData = await api.tratamientos.pagos(t.tratamientoID);
      const avancesData = await api.tratamientos.avances(t.tratamientoID);
      const consolidadoData = await api.tratamientos.consolidado(t.tratamientoID);
      if (!mounted.current) return;
      setPagos(pagosData);
      setAvances(avancesData);
      setConsolidado(consolidadoData);
    } catch {
      if (mounted.current) addToast('error', 'Error al cargar los datos del tratamiento');
    }
  }, [initialTrat.tratamientoID, addToast]);

  useEffect(() => { mounted.current = true; cargarDatos(); return () => { mounted.current = false; }; }, [cargarDatos]);

  const handleAddRow = () => {
    setMaterialRows((prev) => {
      const next = [...prev, { key: `new-${Date.now()}`, materialId: null, nombreMaterial: '', cantidad: 0 }];
      materialRowsRef.current = next;
      return next;
    });
    setDirty(true);
  };

  const handleRemoveRow = (key: string) => {
    setMaterialRows((prev) => {
      const next = prev.filter((r) => r.key !== key);
      materialRowsRef.current = next;
      return next;
    });
    setDirty(true);
  };

  const handleMaterialChange = (key: string, materialId: number) => {
    setMaterialRows((prev) => {
      const next = prev.map((r) => r.key === key ? { ...r, materialId } : r);
      materialRowsRef.current = next;
      return next;
    });
    setDirty(true);
  };

  const handleCantidadChange = (key: string, cantidad: number) => {
    setMaterialRows((prev) => {
      const next = prev.map((r) => r.key === key ? { ...r, cantidad } : r);
      materialRowsRef.current = next;
      return next;
    });
    setDirty(true);
  };

  const handleSaveMaterials = async () => {
    const rows = materialRowsRef.current;
    const original = originalRowsRef.current;
    if (rows.length === 0 && original.length === 0) return;

    setSavingMat(true);
    try {
      const removedKeys = original.filter((or) => !rows.some((r) => r.key === or.key));

      for (const row of removedKeys) {
        const id = Number(row.key.replace('mat-', ''));
        await api.tratamientos.quitarMaterial(id);
      }

      for (const row of rows) {
        if (row.materialId == null) continue;

        if (row.key.startsWith('new-')) {
          if (row.cantidad > 0 || row.materialId != null) {
            await api.tratamientos.agregarMaterial(tratamiento.tratamientoID, {
              materialId: row.materialId,
              cantidad: row.cantidad > 0 ? row.cantidad : 1,
            });
          }
        } else {
          const orig = original.find((or) => or.key === row.key);
          const id = Number(row.key.replace('mat-', ''));

          if (orig && orig.materialId !== row.materialId) {
            await api.tratamientos.quitarMaterial(id);
            await api.tratamientos.agregarMaterial(tratamiento.tratamientoID, {
              materialId: row.materialId,
              cantidad: row.cantidad > 0 ? row.cantidad : 1,
            });
          } else if (orig && orig.cantidad !== row.cantidad) {
            await api.tratamientos.actualizarCantidad(id, row.cantidad);
          }
        }
      }

      addToast('success', 'Materiales guardados correctamente');
      await cargarDatos();
      setDirty(false);
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al guardar materiales');
    } finally {
      setSavingMat(false);
    }
  };

  const handleClose = async () => {
    if (dirty) {
      setSavingMat(true);
      try {
        const rows = materialRowsRef.current;
        const original = originalRowsRef.current;
        const removedKeys = original.filter((or) => !rows.some((r) => r.key === or.key));

        for (const row of removedKeys) {
          const id = Number(row.key.replace('mat-', ''));
          await api.tratamientos.quitarMaterial(id);
        }

        for (const row of rows) {
          if (row.materialId == null) continue;

          if (row.key.startsWith('new-')) {
            if (row.cantidad > 0 || row.materialId != null) {
              await api.tratamientos.agregarMaterial(tratamiento.tratamientoID, {
                materialId: row.materialId,
                cantidad: row.cantidad > 0 ? row.cantidad : 1,
              });
            }
          } else {
            const orig = original.find((or) => or.key === row.key);
            const id = Number(row.key.replace('mat-', ''));

            if (orig && orig.materialId !== row.materialId) {
              await api.tratamientos.quitarMaterial(id);
              await api.tratamientos.agregarMaterial(tratamiento.tratamientoID, {
                materialId: row.materialId,
                cantidad: row.cantidad > 0 ? row.cantidad : 1,
              });
            } else if (orig && orig.cantidad !== row.cantidad) {
              await api.tratamientos.actualizarCantidad(id, row.cantidad);
            }
          }
        }

        addToast('success', 'Materiales guardados automáticamente');
      } catch (err) {
        addToast('error', err instanceof Error ? err.message : 'Error al guardar materiales');
      } finally {
        setSavingMat(false);
      }
    }
    onClose();
  };

  const handleCerrar = async () => { setSaving(true); try { await api.tratamientos.cerrar(tratamiento.tratamientoID); addToast('success', 'Tratamiento cerrado'); onClose(); } catch (err) { addToast('error', err instanceof Error ? err.message : 'Error al cerrar'); } finally { setSaving(false); } };
  const handleAnular = async (motivo?: string) => { if (!motivo) return; try { await api.tratamientos.anular(tratamiento.tratamientoID, motivo); addToast('success', 'Tratamiento anulado'); onClose(); } catch (err) { addToast('error', err instanceof Error ? err.message : 'Error al anular'); } setShowAnular(false); };
  const handleAnularAvance = async (motivo?: string) => { if (!showAnularAvance || !motivo) return; try { await api.tratamientos.anularAvance(showAnularAvance.avanceID, motivo); addToast('success', 'Avance anulado'); await cargarDatos(); } catch (err) { addToast('error', err instanceof Error ? err.message : 'Error al anular avance'); } setShowAnularAvance(null); };
  const handleReabrir = async () => { try { await api.tratamientos.reabrir(tratamiento.tratamientoID); const t = await api.tratamientos.buscarPorId(tratamiento.tratamientoID); setTratamiento(t); addToast('success', 'Tratamiento reabierto'); } catch (err) { addToast('error', err instanceof Error ? err.message : 'Error al reabrir'); } };
  const handleCambiarTipo = async () => { const nuevo = tratamiento.tipo === 'NORMAL' ? 'CONTINUO' : 'NORMAL'; try { await api.tratamientos.cambiarTipo(tratamiento.tratamientoID, nuevo); const t = await api.tratamientos.buscarPorId(tratamiento.tratamientoID); setTratamiento(t); addToast('success', `Tipo cambiado a ${nuevo}`); } catch (err) { addToast('error', err instanceof Error ? err.message : 'Error al cambiar tipo'); } };

  const getEstadoVariant = (e: string) => e === 'CERRADO' ? 'success' : e === 'ANULADO' ? 'danger' : 'info';
  const saldo = tratamiento.monto - tratamiento.montoPagado;
  const totalMat = materialRows.filter((r) => r.materialId != null).length;

  return (
    <>
      <div className="subventana-overlay">
        <div className="subventana">
          <div className="subventana-header">
            <div><h3 className="dialog-title">Detalle de Tratamiento #{tratamiento.tratamientoID}</h3></div>
            <div className="flex flex-center gap-8">
              <Badge variant={getEstadoVariant(tratamiento.estado)}>{tratamiento.estado}</Badge>
              <button className="btn btn-ghost btn-sm" onClick={handleClose}><X size={18} /></button>
            </div>
          </div>
          <div className="subventana-body">
            <div className="sv-grid">
              <div className="sv-row"><span className="sv-label">Operador</span><span>{operadorNombre ?? `#${tratamiento.operadorID}`}</span></div>
              <div className="sv-row"><span className="sv-label">Paciente</span><span>{pacienteNombre ?? `#${tratamiento.pacienteID}`}</span></div>
              <div className="sv-row"><span className="sv-label">Fecha</span><span>{tratamiento.fecha}</span></div>
              <div className="sv-row"><span className="sv-label">Monto total</span><span className="num">{formatMonto(tratamiento.monto)}</span></div>
              <div className="sv-row"><span className="sv-label">Monto pagado</span><span className="num">{formatMonto(tratamiento.montoPagado)}</span></div>
              <div className="sv-row"><span className="sv-label">Tipo</span><Badge variant="info">{tratamiento.tipo}</Badge></div>
              <div className="sv-row"><span className="sv-label">Pago</span><Badge variant={tratamiento.estadoPago === 'PAGADO' ? 'success' : tratamiento.estadoPago === 'PARCIAL' ? 'warning' : 'neutral'}>{tratamiento.estadoPago}</Badge></div>
            </div>
            {saldo > 0 && tratamiento.estado === 'ABIERTO' && (
              <div className="alert-banner alert-warning mt-16"><AlertTriangle size={16} /><span>Saldo pendiente: {formatMonto(saldo)}</span></div>
            )}
            {pagos.length > 0 && (
              <div className="mt-16">
                <h4 className="mb-12">Pagos</h4>
                <ul className="material-list">
                  {pagos.map((p) => (
                    <li key={p.pagoID} className="material-list-item">
                      <span className="material-list-name">{p.fecha}</span>
                      <span className="material-list-cant">{formatMonto(p.monto)}</span>
                    </li>
                  ))}
                </ul>
              </div>
            )}
            {avances.length > 0 && (
              <div className="mt-16">
                <h4 className="mb-12">Avances</h4>
                <ul className="material-list">
                  {avances.map((a) => (
                    <li key={a.avanceID} className="material-list-item">
                      <span className="material-list-name">#{a.avanceID} - {a.fecha}</span>
                      <Badge variant={a.estado === 'ANULADO' ? 'danger' : 'info'}>{a.estado}</Badge>
                      {a.estado === 'ACTIVO' && (
                        <button className="btn btn-ghost btn-sm" onClick={() => setShowAnularAvance(a)}><AlertTriangle size={14} /></button>
                      )}
                    </li>
                  ))}
                </ul>
              </div>
            )}
            {consolidado && consolidado.materiales.length > 0 && (
              <div className="mt-16">
                <h4 className="mb-12">Materiales consolidados</h4>
                <ul className="material-list">
                  {consolidado.materiales.map((m) => (
                    <li key={m.materialID} className="material-list-item">
                      <span className="material-list-name">{m.nombreMaterial}</span>
                      <span className="material-list-cant">{m.cantidad} {m.unidad}</span>
                    </li>
                  ))}
                </ul>
              </div>
            )}
            <h4 className="mt-20 mb-12">Materiales del tratamiento</h4>
            <MaterialTable rows={materialRows} materials={materiales.data ?? []} onAdd={handleAddRow} onRemove={handleRemoveRow} onMaterialChange={handleMaterialChange} onCantidadChange={handleCantidadChange} />
          </div>
          <div className="subventana-footer">
            <span className="text-muted text-sm">
              {totalMat} material(es){dirty && <span className="text-warning ml-8">(cambios sin guardar)</span>}
            </span>
            <div className="flex gap-8 flex-wrap">
              {dirty && (
                <button className="btn btn-primary" onClick={handleSaveMaterials} disabled={savingMat}>
                  {savingMat ? 'Guardando...' : 'Guardar materiales'}
                </button>
              )}
              {tratamiento.estado === 'ABIERTO' && (
                <>
                  <button className="btn btn-success" onClick={handleCerrar} disabled={saving}>Cerrar tratamiento</button>
                  <button className="btn btn-primary" onClick={() => setShowPago(true)}><DollarSign size={14} /> Registrar pago</button>
                  <button className="btn btn-secondary" onClick={() => setShowAvance(true)}><Plus size={14} /> Registrar avance</button>
                  <button className="btn btn-secondary" onClick={handleCambiarTipo}><ArrowLeftRight size={14} /> {tratamiento.tipo === 'NORMAL' ? 'CONTINUO' : 'NORMAL'}</button>
                  <button className="btn btn-danger" onClick={() => setShowAnular(true)}><AlertTriangle size={14} /> Anular</button>
                </>
              )}
              {tratamiento.estado === 'CERRADO' && (
                <>
                  <button className="btn btn-secondary" onClick={handleReabrir}><RotateCcw size={14} /> Reabrir</button>
                  <button className="btn btn-primary" onClick={() => setShowPago(true)}><DollarSign size={14} /> Registrar pago</button>
                  <button className="btn btn-secondary" onClick={() => setShowAvance(true)}><Plus size={14} /> Registrar avance</button>
                  <button className="btn btn-danger" onClick={() => setShowAnular(true)}><AlertTriangle size={14} /> Anular</button>
                </>
              )}
            </div>
          </div>
        </div>
      </div>
      {showPago && (
        <RegistrarPagoModal
          tratamiento={tratamiento}
          onClose={() => setShowPago(false)}
          onSuccess={async () => {
            const t = await api.tratamientos.buscarPorId(tratamiento.tratamientoID);
            setTratamiento(t);
            setShowPago(false);
          }}
          addToast={addToast}
        />
      )}
      {showAvance && (
        <RegistrarAvanceModal
          tratamiento={tratamiento}
          onClose={() => setShowAvance(false)}
          onSuccess={async () => {
            await cargarDatos();
            setShowAvance(false);
          }}
          addToast={addToast}
        />
      )}
      <ConfirmDialog open={showAnular} title="Anular tratamiento" message={`Confirme que desea anular el tratamiento #${tratamiento.tratamientoID}.`} confirmLabel="Si, anular" variant="danger" requireMotivo onConfirm={handleAnular} onCancel={() => setShowAnular(false)} />
      <ConfirmDialog open={!!showAnularAvance} title="Anular avance" message={showAnularAvance ? `Confirme que desea anular el avance #${showAnularAvance.avanceID} (${showAnularAvance.fecha}). El pago asociado, si existe, será eliminado.` : ''} confirmLabel="Si, anular" variant="danger" requireMotivo onConfirm={handleAnularAvance} onCancel={() => setShowAnularAvance(null)} />
    </>
  );
}
