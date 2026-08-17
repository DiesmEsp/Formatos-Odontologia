import { useState } from 'react';
import { Plus, Pencil, Trash2 } from 'lucide-react';
import { useApi } from '../hooks/useApi';
import { useToast } from '../hooks/useToast';
import { api } from '../api';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { CatalogoModal } from '../components/CatalogoModal';
import { Badge } from '../components/Badge';
import type { Clinica } from '../api/types';

const CAMPOS_CLINICA = [
  { key: 'nombre', label: 'Nombre', type: 'text' as const },
  { key: 'grupo', label: 'Grupo (opcional)', type: 'text' as const },
];

export default function Clinicas() {
  const clinicas = useApi(() => api.clinicas.listar());
  const { addToast } = useToast();

  const [modalAbierto, setModalAbierto] = useState(false);
  const [editando, setEditando] = useState<Clinica | null>(null);
  const [saving, setSaving] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<Clinica | null>(null);

  const abrirNueva = () => {
    setEditando(null);
    setModalAbierto(true);
  };

  const abrirEdicion = (clinica: Clinica) => {
    setEditando(clinica);
    setModalAbierto(true);
  };

  const guardar = async (values: Record<string, any>) => {
    const nombre = (values.nombre ?? '').toString().trim();
    const grupo = (values.grupo ?? '').toString().trim() || null;
    if (!nombre) {
      addToast('warning', 'Indique el nombre de la clinica.');
      return;
    }
    setSaving(true);
    try {
      if (editando) {
        await api.clinicas.actualizar({ ...editando, nombre, grupo });
        addToast('success', 'Clinica actualizada.');
      } else {
        await api.clinicas.crear({ nombre, grupo });
        addToast('success', 'Clinica creada.');
      }
      clinicas.refetch();
      setModalAbierto(false);
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'No se pudo guardar la clinica.');
    } finally {
      setSaving(false);
    }
  };

  const eliminar = async () => {
    if (!deleteTarget) return;
    try {
      await api.clinicas.eliminar(deleteTarget.clinicaID);
      addToast('success', `Clinica "${deleteTarget.nombre}" eliminada.`);
      clinicas.refetch();
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'No se pudo eliminar la clinica.');
    } finally {
      setDeleteTarget(null);
    }
  };

  const lista = clinicas.data ?? [];

  return (
    <div>
      <div className="view-header">
        <div>
          <h1 className="view-title">Gestion de Clinicas</h1>
          <p className="view-subtitle">Cree, edite y elimine las clinicas del sistema</p>
        </div>
        <button className="btn btn-primary" onClick={abrirNueva}>
          <Plus size={16} />
          <span>Nueva Clinica</span>
        </button>
      </div>

      <div className="card">
        {clinicas.loading ? (
          <div className="empty-state"><span className="empty-text">Cargando clinicas...</span></div>
        ) : lista.length === 0 ? (
          <div className="empty-state">
            <span className="empty-title">No hay clinicas registradas</span>
            <span className="empty-text">Cree la primera clinica para comenzar.</span>
          </div>
        ) : (
          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th style={{ width: 60 }}>#</th>
                  <th>Nombre</th>
                  <th>Grupo</th>
                  <th style={{ width: 100 }}>Estado</th>
                  <th style={{ width: 120 }} className="text-center">Acciones</th>
                </tr>
              </thead>
              <tbody>
                {lista.map((c) => (
                  <tr key={c.clinicaID}>
                    <td className="num">{c.clinicaID}</td>
                    <td>{c.nombre}</td>
                    <td>{c.grupo ?? '—'}</td>
                    <td>
                      {c.estado === 1 ? (
                        <Badge variant="success">Activa</Badge>
                      ) : (
                        <Badge variant="neutral">Inactiva</Badge>
                      )}
                    </td>
                    <td className="text-center">
                      <button className="btn btn-ghost btn-sm" onClick={() => abrirEdicion(c)} title="Editar clinica">
                        <Pencil size={14} />
                      </button>
                      <button
                        className="btn btn-ghost btn-sm"
                        onClick={() => setDeleteTarget(c)}
                        title="Eliminar clinica"
                      >
                        <Trash2 size={14} />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <CatalogoModal
        open={modalAbierto}
        title={editando ? 'Editar Clinica' : 'Nueva Clinica'}
        fields={CAMPOS_CLINICA}
        initialValues={editando ? { nombre: editando.nombre, grupo: editando.grupo ?? '' } : { nombre: '', grupo: '' }}
        onSave={guardar}
        onCancel={() => setModalAbierto(false)}
        saving={saving}
      />

      <ConfirmDialog
        open={!!deleteTarget}
        title="Eliminar clinica"
        message={
          deleteTarget
            ? `Confirme que desea eliminar la clinica "${deleteTarget.nombre}". Si tiene registros asociados, no se podra eliminar.`
            : ''
        }
        confirmLabel="Eliminar"
        variant="danger"
        onConfirm={eliminar}
        onCancel={() => setDeleteTarget(null)}
      />
    </div>
  );
}