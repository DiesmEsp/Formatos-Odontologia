import { useState } from 'react';
import { SearchableCombo, type SearchableOption } from '../components/SearchableCombo';
import { MaterialTable, type MaterialRow } from '../components/MaterialTable';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { Badge } from '../components/Badge';
import { useApi } from '../hooks/useApi';
import { useToast } from '../hooks/useToast';
import { api } from '../api';
import { hoyISO, nombreCompleto } from '../lib/format';
import type { Asistencia } from '../api/types';

export default function Asistencia() {
  const [q, setQ] = useState('');
  const [selectedDocente, setSelectedDocente] = useState<SearchableOption | null>(null);
  const [fecha, setFecha] = useState(hoyISO());
  const [asistencia, setAsistencia] = useState<Asistencia | null>(null);
  const [materialRows, setMaterialRows] = useState<MaterialRow[]>([]);
  const [showAnular, setShowAnular] = useState(false);
  const { addToast } = useToast();

  const docentes = useApi(() => api.catalogos.docentes.listar(q || undefined), [q]);
  const materiales = useApi(() => api.catalogos.materiales.listar());

  const docenteOptions: SearchableOption[] = (docentes.data ?? []).map((d) => ({
    id: d.docenteID,
    label: nombreCompleto(d.nombres, d.apellidos),
    extra: d.telefono,
  }));

  const handleSelectDocente = async (id: number | null) => {
    if (id === null) return;
    const opt = docenteOptions.find((d) => d.id === id);
    setSelectedDocente(opt ?? null);
    try {
      const a = await api.asistencia.abrirDia({ docenteId: id, fecha });
      setAsistencia(a);
      const mats = await api.asistencia.materialesDelDia(a.asistenciaID);
      setMaterialRows(mats.map((m, i) => ({
        key: `existing-${m.materialesListID ?? i}`,
        materialId: m.materialID,
        nombreMaterial: m.nombreMaterial,
        cantidad: m.cantidad,
      })));
      addToast('success', `Asistencia abierta para ${opt?.label}`);
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al abrir asistencia');
    }
  };

  const handleAddMaterial = () => {
    setMaterialRows((prev) => [...prev, { key: `new-${Date.now()}`, materialId: null, nombreMaterial: '', cantidad: 0 }]);
  };

  const handleRemoveMaterial = (key: string) => {
    setMaterialRows((prev) => prev.filter((r) => r.key !== key));
  };

  const handleMaterialChange = async (key: string, materialId: number) => {
    setMaterialRows((prev) => prev.map((r) => r.key === key ? { ...r, materialId } : r));
    if (asistencia) {
      const row = materialRows.find((r) => r.key === key);
      if (row?.materialId) {
        try {
          await api.asistencia.acumularMaterial(asistencia.asistenciaID, { materialId, cantidad: row.cantidad });
        } catch {}
      }
    }
  };

  const handleCantidadChange = async (key: string, cantidad: number) => {
    setMaterialRows((prev) => prev.map((r) => r.key === key ? { ...r, cantidad } : r));
    if (asistencia) {
      const row = materialRows.find((r) => r.key === key);
      if (row?.materialId) {
        try {
          await api.asistencia.acumularMaterial(asistencia.asistenciaID, { materialId: row.materialId, cantidad });
        } catch {}
      }
    }
  };

  const handleAnular = async (motivo?: string) => {
    if (!asistencia || !motivo) return;
    try {
      await api.asistencia.anular(asistencia.asistenciaID, motivo);
      addToast('success', 'Asistencia anulada correctamente');
      setAsistencia(null);
      setMaterialRows([]);
      setSelectedDocente(null);
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al anular');
    }
    setShowAnular(false);
  };

  return (
    <div>
      <div className="view-header">
        <h1 className="view-title">Asistencia Docente</h1>
        <p className="view-subtitle">Entrega y control de materiales a docentes</p>
      </div>

      <div className="card" style={{ marginBottom: 20 }}>
        <div className="card-header">
          <h3 className="card-title">1. Seleccionar Docente</h3>
        </div>
        <div style={{ display: 'flex', gap: 12, alignItems: 'flex-end', flexWrap: 'wrap' }}>
          <div style={{ flex: 1, maxWidth: 400 }}>
            <label className="form-label">Docente</label>
            <SearchableCombo
              options={docenteOptions}
              value={selectedDocente?.id ?? null}
              onChange={handleSelectDocente}
              onSearch={setQ}
              placeholder="Buscar docente por nombre..."
            />
          </div>
          <div>
            <label className="form-label">Fecha</label>
            <input type="date" className="text-field" value={fecha} onChange={(e) => setFecha(e.target.value)} style={{ width: 160 }} />
          </div>
        </div>
      </div>

      {asistencia && selectedDocente && (
        <div className="card">
          <div className="card-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <h3 className="card-title">2. Registro del dia — {selectedDocente.label} — {fecha}</h3>
            <Badge variant="success">Registro activo</Badge>
          </div>

          <div className="alert-banner alert-info" style={{ marginBottom: 16 }}>
            Este registro acumula todos los materiales del dia. No es necesario crear uno nuevo si el docente vuelve a pedir.
          </div>

          <MaterialTable
            rows={materialRows}
            materials={materiales.data ?? []}
            onAdd={handleAddMaterial}
            onRemove={handleRemoveMaterial}
            onMaterialChange={handleMaterialChange}
            onCantidadChange={handleCantidadChange}
          />

          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 16, paddingTop: 16, borderTop: '1px solid var(--color-border)' }}>
            <span className="text-muted text-sm">{materialRows.length} materiales registrados hoy</span>
            <div style={{ display: 'flex', gap: 8 }}>
              <button className="btn btn-danger" onClick={() => setShowAnular(true)}>
                Anular asistencia
              </button>
            </div>
          </div>
        </div>
      )}

      <ConfirmDialog
        open={showAnular}
        title="Anular asistencia docente"
        message={`Confirme que desea anular la asistencia de ${selectedDocente?.label} del dia ${fecha}.`}
        confirmLabel="Si, anular asistencia"
        variant="danger"
        requireMotivo
        onConfirm={handleAnular}
        onCancel={() => setShowAnular(false)}
      />
    </div>
  );
}
