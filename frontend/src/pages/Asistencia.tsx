import { useState, useCallback } from 'react';
import { SearchableCombo, type SearchableOption } from '../components/SearchableCombo';
import { MaterialTable, type MaterialRow } from '../components/MaterialTable';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { Badge } from '../components/Badge';
import { useApi } from '../hooks/useApi';
import { useToast } from '../hooks/useToast';
import { api } from '../api';
import { hoyISO, nombreCompleto } from '../lib/format';
import type { Asistencia } from '../api/types';
import materialesDefaultData from '../data/materiales-docente-default.json';

export default function Asistencia() {
  const [q, setQ] = useState('');
  const [selectedDocente, setSelectedDocente] = useState<SearchableOption | null>(null);
  const [fecha, setFecha] = useState(hoyISO());
  const [asistencia, setAsistencia] = useState<Asistencia | null>(null);
  const [materialRows, setMaterialRows] = useState<MaterialRow[]>([]);
  const [showAnular, setShowAnular] = useState(false);
  const { addToast } = useToast();

  const asistenciaHoy = useApi(() => api.dashboard.asistenciaHoy());
  const docentes = useApi(() => api.catalogos.docentes.listar(q || undefined), [q]);
  const materiales = useApi(() => api.catalogos.materiales.listar());

  const docenteOptions: SearchableOption[] = (docentes.data ?? []).map((d) => ({
    id: d.docenteID,
    label: nombreCompleto(d.nombres, d.apellidos),
  }));

  const abrirAsistencia = useCallback(async (docenteId: number, nombre: string) => {
    try {
      const a = await api.asistencia.abrirDia({ docenteId, fecha });
      setAsistencia(a);
      setSelectedDocente({ id: docenteId, label: nombre });

      const existingMats = await api.asistencia.materialesDelDia(a.asistenciaID);

      if (existingMats.length > 0) {
        setMaterialRows(existingMats.map((m, i) => ({
          key: `existing-${m.materialesListID ?? i}`,
          materialId: m.materialID, nombreMaterial: m.nombreMaterial, cantidad: m.cantidad,
        })));
      } else {
        const defaultMats: MaterialRow[] = materialesDefaultData.materiales.map((dm, i) => ({
          key: `default-${i}-${Date.now()}`,
          materialId: dm.materialId,
          nombreMaterial: '',
          cantidad: dm.cantidad,
        }));

        for (const dm of defaultMats) {
          if (dm.materialId) {
            try {
              await api.asistencia.acumularMaterial(a.asistenciaID, { materialId: dm.materialId, cantidad: dm.cantidad });
            } catch {}
          }
        }

      setMaterialRows(defaultMats);
    }

    addToast('success', `Asistencia abierta para ${nombre}`);
    asistenciaHoy.refetch();
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al abrir asistencia');
    }
  }, [fecha, addToast, asistenciaHoy]);

  const handleSelectDocente = async (id: number | null) => {
    if (id === null) return;
    const opt = docenteOptions.find((d) => d.id === id);
    if (opt) await abrirAsistencia(id, opt.label);
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
      if (row?.cantidad && row.cantidad > 0) {
        try { await api.asistencia.acumularMaterial(asistencia.asistenciaID, { materialId, cantidad: row.cantidad }); } catch {}
      }
    }
  };

  const handleCantidadChange = async (key: string, cantidad: number) => {
    setMaterialRows((prev) => prev.map((r) => r.key === key ? { ...r, cantidad } : r));
    if (asistencia) {
      const row = materialRows.find((r) => r.key === key);
      if (row?.materialId && cantidad > 0) {
        try { await api.asistencia.acumularMaterial(asistencia.asistenciaID, { materialId: row.materialId, cantidad }); } catch {}
      }
    }
  };

  const handleAnular = async (motivo?: string) => {
    if (!asistencia || !motivo) return;
    try {
      await api.asistencia.anular(asistencia.asistenciaID, motivo);
      addToast('success', 'Asistencia anulada correctamente');
      setAsistencia(null); setMaterialRows([]); setSelectedDocente(null);
      asistenciaHoy.refetch();
    } catch (err) { addToast('error', err instanceof Error ? err.message : 'Error al anular'); }
    setShowAnular(false);
  };

  const handleRestaurarDefault = async () => {
    if (!asistencia) return;
    const defaultMats: MaterialRow[] = materialesDefaultData.materiales.map((dm, i) => ({
      key: `default-${i}-${Date.now()}`,
      materialId: dm.materialId,
      nombreMaterial: '',
      cantidad: dm.cantidad,
    }));
    setMaterialRows(defaultMats);
    for (const dm of defaultMats) {
      if (dm.materialId) {
        try { await api.asistencia.acumularMaterial(asistencia.asistenciaID, { materialId: dm.materialId, cantidad: dm.cantidad }); } catch {}
      }
    }
    addToast('info', 'Lista de materiales restaurada al predeterminado');
  };

  return (
    <div>
      <div className="view-header">
        <h1 className="view-title">Asistencia Docente</h1>
        <p className="subtitle">Entrega y control de materiales a docentes</p>
      </div>

      <div className="card">
        <div className="card-header">
          <h3 className="card-title">Docentes registrados</h3>
        </div>
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Nombre</th>
                <th>Apellidos</th>
                <th style={{ width: 120 }}>Estado</th>
                <th style={{ width: 120 }} />
              </tr>
            </thead>
            <tbody>
              {(asistenciaHoy.data ?? []).map((d) => (
                <tr key={d.docenteID}>
                  <td>{d.nombres}</td>
                  <td>{d.apellidos}</td>
                  <td>
                    {d.presente ? (
                      <Badge variant="success"><span className="led led-ok" />Presente</Badge>
                    ) : (
                      <Badge variant="neutral"><span className="led led-danger" />Ausente</Badge>
                    )}
                  </td>
                  <td>
                    {d.presente ? (
                      <button className="btn btn-ghost btn-sm" onClick={() => abrirAsistencia(d.docenteID, `${d.nombres} ${d.apellidos}`)}>
                        Ver materiales
                      </button>
                    ) : (
                      <button className="btn btn-primary btn-sm" onClick={() => abrirAsistencia(d.docenteID, `${d.nombres} ${d.apellidos}`)}>
                        Registrar
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <div className="card" style={{ marginBottom: 20 }}>
        <div className="card-header">
          <h3 className="card-title">Busqueda manual</h3>
        </div>
        <div style={{ display: 'flex', gap: 12, alignItems: 'flex-end', flexWrap: 'wrap' }}>
          <div style={{ flex: 1, maxWidth: 400 }}>
            <label className="form-label">Docente</label>
            <SearchableCombo options={docenteOptions} value={selectedDocente?.id ?? null} onChange={handleSelectDocente} onSearch={setQ} placeholder="Buscar docente por nombre..." />
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
            <h3 className="card-title">Registro del dia — {selectedDocente.label} — {fecha}</h3>
            <Badge variant="success"><span className="led led-ok" />Registro activo</Badge>
          </div>

          <div className="alert-banner alert-info" style={{ marginBottom: 16 }}>
            Este registro acumula todos los materiales del dia. Puede agregar mas materiales segun el docente solicite.
          </div>

          <MaterialTable rows={materialRows} materials={materiales.data ?? []} onAdd={handleAddMaterial} onRemove={handleRemoveMaterial} onMaterialChange={handleMaterialChange} onCantidadChange={handleCantidadChange} />

          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 16, paddingTop: 16, borderTop: '1px solid var(--color-border)' }}>
            <span className="text-muted text-sm">{materialRows.length} materiales registrados hoy</span>
            <div style={{ display: 'flex', gap: 8 }}>
              <button className="btn btn-ghost btn-sm" onClick={handleRestaurarDefault}>Restaurar lista predeterminada</button>
              <button className="btn btn-danger btn-sm" onClick={() => setShowAnular(true)}>Anular asistencia</button>
            </div>
          </div>
        </div>
      )}

      <ConfirmDialog open={showAnular} title="Anular asistencia docente"
        message={`Confirme que desea anular la asistencia de ${selectedDocente?.label} del dia ${fecha}.`}
        confirmLabel="Si, anular asistencia" variant="danger" requireMotivo
        onConfirm={handleAnular} onCancel={() => setShowAnular(false)} />
    </div>
  );
}
