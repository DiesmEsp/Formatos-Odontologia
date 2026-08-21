import { useState } from 'react';
import { Plus, Pencil, Trash2, X } from 'lucide-react';
import { useApi } from '../hooks/useApi';
import { useToast } from '../hooks/useToast';
import { api } from '../api';
import type { ConsumoMaterial } from '../api/types';
import { MaterialTable, type MaterialRow } from '../components/MaterialTable';
import { SearchableCombo } from '../components/SearchableCombo';
import { MonthYearPicker } from '../components/MonthYearPicker';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { CatalogoTabla, type Column } from '../components/CatalogoTabla';
import { formatFecha, hoyISO, mesActual, anioActual } from '../lib/format';

export default function Consumos() {
  const { addToast } = useToast();
  const [mes, setMes] = useState(mesActual());
  const [anio, setAnio] = useState(anioActual());
  const [modalNuevo, setModalNuevo] = useState(false);
  const [editTarget, setEditTarget] = useState<ConsumoMaterial | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<ConsumoMaterial | null>(null);

  const data = useApi(() => api.consumos.listar(anio, mes), [anio, mes]);
  const list = data.data ?? [];

  const handleEliminar = async () => {
    if (!deleteTarget) return;
    try {
      await api.consumos.eliminar(deleteTarget.consumoID);
      addToast('success', 'Consumo eliminado');
      data.refetch();
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al eliminar');
    } finally {
      setDeleteTarget(null);
    }
  };

  const columns: Column<ConsumoMaterial>[] = [
    { key: "fecha", header: "Fecha", width: 120, render: (r) => formatFecha(r.fecha), sortValue: (r) => r.fecha },
    { key: "material", header: "Material", render: (r) => r.nombreMaterial, sortValue: (r) => r.nombreMaterial },
    { key: "unidad", header: "Unidad", width: 110, render: (r) => r.unidad },
    { key: "cantidad", header: "Cantidad", width: 100, render: (r) => <span className="num">{r.cantidad}</span>, sortValue: (r) => r.cantidad },
    { key: "acciones", header: "", width: 80, className: "text-center", render: (r) => (
      <div className="flex gap-4 justify-center">
        <button className="btn btn-ghost btn-sm" onClick={() => setEditTarget(r)} aria-label={`Editar consumo ${r.consumoID}`}><Pencil size={14} /></button>
        <button className="btn btn-ghost btn-sm" onClick={() => setDeleteTarget(r)} aria-label={`Eliminar consumo ${r.consumoID}`}><Trash2 size={14} /></button>
      </div>
    )},
  ];

  return (
    <div>
      <div className="view-header">
        <h1 className="view-title">Materiales Consumidos</h1>
        <p className="view-subtitle">Registro de materiales consumidos directamente por la clinica</p>
      </div>

      <div className="card" style={{ marginBottom: 20 }}>
        <h3 className="card-title" style={{ marginBottom: 14 }}>Periodo</h3>
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

      <div className="card">
        <div style={{ marginBottom: 12, display: "flex", justifyContent: "flex-end" }}>
          <button className="btn btn-primary btn-sm" onClick={() => setModalNuevo(true)}>
            <Plus size={14} /> Nuevo Consumo
          </button>
        </div>
        <CatalogoTabla
          columns={columns}
          data={list}
          loading={data.loading}
          searchEnabled={false}
          rowKey={(r) => r.consumoID}
          emptyTitle="Sin consumos registrados"
          emptyText={`No hay materiales consumidos registrados en ${mes}/${anio}.`}
        />
      </div>

      {modalNuevo && (
        <NuevoConsumoModal
          onClose={() => setModalNuevo(false)}
          onSaved={() => { setModalNuevo(false); data.refetch(); }}
          addToast={addToast}
        />
      )}

      {editTarget && (
        <EditarConsumoModal
          target={editTarget}
          onClose={() => setEditTarget(null)}
          onSaved={() => { setEditTarget(null); data.refetch(); }}
          addToast={addToast}
        />
      )}

      <ConfirmDialog
        open={!!deleteTarget}
        title="Eliminar consumo"
        message={`Confirme que desea eliminar el registro de "${deleteTarget?.nombreMaterial}" del ${formatFecha(deleteTarget?.fecha ?? '')}.`}
        confirmLabel="Eliminar"
        variant="danger"
        onConfirm={handleEliminar}
        onCancel={() => setDeleteTarget(null)}
      />
    </div>
  );
}

function rowsToPayload(fecha: string, rows: MaterialRow[]) {
  return rows
    .filter((r) => r.materialId != null && r.cantidad > 0)
    .map((r) => ({ fecha, materialId: r.materialId!, cantidad: r.cantidad }));
}

function NuevoConsumoModal({
  onClose, onSaved, addToast,
}: {
  onClose: () => void;
  onSaved: () => void;
  addToast: ReturnType<typeof useToast>['addToast'];
}) {
  const [fecha, setFecha] = useState(hoyISO());
  const [rows, setRows] = useState<MaterialRow[]>([]);
  const [saving, setSaving] = useState(false);
  const materiales = useApi(() => api.catalogos.materiales.listar());

  const handleAddRow = () => setRows((prev) => [...prev, { key: `new-${Date.now()}`, materialId: null, nombreMaterial: '', cantidad: 0 }]);
  const handleRemoveRow = (key: string) => setRows((prev) => prev.filter((r) => r.key !== key));
  const handleMaterialChange = (key: string, materialId: number) => setRows((prev) => prev.map((r) => r.key === key ? { ...r, materialId } : r));
  const handleCantidadChange = (key: string, cantidad: number) => setRows((prev) => prev.map((r) => r.key === key ? { ...r, cantidad } : r));

  const handleGuardar = async () => {
    const items = rowsToPayload(fecha, rows);
    if (!/^\d{4}-\d{2}-\d{2}$/.test(fecha)) {
      addToast('error', 'Seleccione una fecha válida');
      return;
    }
    if (items.length === 0) {
      addToast('error', 'Agregue al menos un material con cantidad mayor a cero');
      return;
    }
    setSaving(true);
    try {
      await api.consumos.crearLote(items);
      addToast('success', `${items.length} consumo(s) registrado(s)`);
      onSaved();
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al registrar consumos');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="dialog-overlay" onClick={onClose}>
      <div className="dialog-pane mw-560" role="dialog" aria-modal="true" onClick={(e) => e.stopPropagation()}>
        <div className="dialog-header">
          <h3 className="dialog-title">Nuevo Consumo de Materiales</h3>
          <button className="btn btn-ghost btn-sm dialog-close" onClick={onClose}><X size={18} /></button>
        </div>
        <div className="dialog-body">
          <div className="form-group">
            <label className="form-label">Fecha</label>
            <input type="date" className="text-field w-full" value={fecha} onChange={(e) => setFecha(e.target.value)} />
          </div>
          <div className="form-group">
            <label className="form-label">Materiales consumidos</label>
            <MaterialTable
              rows={rows}
              materials={materiales.data ?? []}
              onAdd={handleAddRow}
              onRemove={handleRemoveRow}
              onMaterialChange={handleMaterialChange}
              onCantidadChange={handleCantidadChange}
              loading={materiales.loading}
            />
          </div>
        </div>
        <div className="dialog-footer">
          <button className="btn btn-secondary" onClick={onClose}>Cancelar</button>
          <button className="btn btn-primary" onClick={handleGuardar} disabled={saving}>
            {saving ? 'Guardando...' : 'Guardar'}
          </button>
        </div>
      </div>
    </div>
  );
}

function EditarConsumoModal({
  target, onClose, onSaved, addToast,
}: {
  target: ConsumoMaterial;
  onClose: () => void;
  onSaved: () => void;
  addToast: ReturnType<typeof useToast>['addToast'];
}) {
  const [fecha, setFecha] = useState(target.fecha);
  const [materialId, setMaterialId] = useState<number | null>(target.materialID);
  const [cantidadStr, setCantidadStr] = useState(String(target.cantidad));
  const [saving, setSaving] = useState(false);
  const materiales = useApi(() => api.catalogos.materiales.listar());

  const options = (materiales.data ?? []).map((m) => ({ id: m.materialID, label: m.nombre, extra: m.unidad }));

  const filterNumeric = (val: string) => {
    let cleaned = val.replace(/[^0-9.]/g, '');
    const parts = cleaned.split('.');
    if (parts.length > 2) cleaned = parts[0] + '.' + parts.slice(1).join('');
    return cleaned;
  };

  const handleGuardar = async () => {
    if (!materialId) { addToast('error', 'Seleccione un material'); return; }
    const cantidad = Number(cantidadStr);
    if (!(cantidad > 0)) { addToast('error', 'La cantidad debe ser mayor a cero'); return; }
    if (!/^\d{4}-\d{2}-\d{2}$/.test(fecha)) { addToast('error', 'Seleccione una fecha válida'); return; }
    setSaving(true);
    try {
      await api.consumos.actualizar(target.consumoID, { fecha, materialId, cantidad });
      addToast('success', 'Consumo actualizado');
      onSaved();
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al actualizar');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="dialog-overlay" onClick={onClose}>
      <div className="dialog-pane mw-560" role="dialog" aria-modal="true" onClick={(e) => e.stopPropagation()}>
        <div className="dialog-header">
          <h3 className="dialog-title">Editar Consumo</h3>
          <button className="btn btn-ghost btn-sm dialog-close" onClick={onClose}><X size={18} /></button>
        </div>
        <div className="dialog-body">
          <div className="form-group">
            <label className="form-label">Fecha</label>
            <input type="date" className="text-field w-full" value={fecha} onChange={(e) => setFecha(e.target.value)} />
          </div>
          <div className="form-group">
            <label className="form-label">Material</label>
            <SearchableCombo
              options={options}
              value={materialId}
              onChange={setMaterialId}
              placeholder="Buscar material..."
              loading={materiales.loading}
            />
          </div>
          <div className="form-group">
            <label className="form-label">Cantidad</label>
            <input
              type="text"
              inputMode="decimal"
              className="text-field w-full"
              value={cantidadStr}
              onChange={(e) => setCantidadStr(filterNumeric(e.target.value))}
              min={0}
            />
          </div>
        </div>
        <div className="dialog-footer">
          <button className="btn btn-secondary" onClick={onClose}>Cancelar</button>
          <button className="btn btn-primary" onClick={handleGuardar} disabled={saving}>
            {saving ? 'Guardando...' : 'Guardar cambios'}
          </button>
        </div>
      </div>
    </div>
  );
}
