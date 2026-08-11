import { useState, useCallback } from 'react';
import { Plus, Trash2 } from 'lucide-react';
import { useApi } from '../hooks/useApi';
import { useToast } from '../hooks/useToast';
import { api } from '../api';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { Badge } from '../components/Badge';

export default function Unidades() {
  const unidades = useApi(() => api.unidades.listar());
  const tratamientos = useApi(() => api.tratamientos.activos());
  const { addToast } = useToast();

  const [deleteTarget, setDeleteTarget] = useState<{ id: number; nro: number } | null>(null);
  const [creating, setCreating] = useState(false);

  const tratamientosActivos = tratamientos.data ?? [];

  const isOcupada = useCallback((unidadId: number) => {
    return tratamientosActivos.some((t) => t.unidadID === unidadId && t.estado !== 'CERRADO' && t.estado !== 'ANULADO');
  }, [tratamientosActivos]);

  const handleCreate = async () => {
    setCreating(true);
    try {
      await api.unidades.crear();
      addToast('success', 'Unidad creada correctamente');
      unidades.refetch();
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al crear unidad');
    } finally {
      setCreating(false);
    }
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;
    try {
      await api.unidades.eliminar(deleteTarget.id);
      addToast('success', `Unidad ${deleteTarget.nro} eliminada`);
      unidades.refetch();
    } catch (err) {
      addToast('error', err instanceof Error ? err.message : 'Error al eliminar unidad');
    } finally {
      setDeleteTarget(null);
    }
  };

  const lista = unidades.data ?? [];
  const libres = lista.filter((u) => !isOcupada(u.unidadID)).length;
  const ocupadas = lista.length - libres;

  return (
    <div>
      <div className="view-header">
        <div>
          <h1 className="view-title">Gestion de Unidades</h1>
          <p className="view-subtitle">Administre las unidades de atención de la clínica</p>
        </div>
        <button className="btn btn-primary" onClick={handleCreate} disabled={creating}>
          <Plus size={16} />
          <span>Nueva Unidad</span>
        </button>
      </div>

      <div className="card">
        {unidades.loading ? (
          <div className="empty-state"><span className="empty-text">Cargando unidades...</span></div>
        ) : lista.length === 0 ? (
          <div className="empty-state">
            <span className="empty-title">No hay unidades registradas</span>
            <span className="empty-text">Cree la primera unidad para comenzar.</span>
          </div>
        ) : (
          <>
            <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th style={{ width: 80 }}>#</th>
                  <th>Nombre</th>
                  <th style={{ width: 120 }}>Estado</th>
                  <th style={{ width: 80 }} className="text-center">Accion</th>
                </tr>
              </thead>
              <tbody>
                {lista.map((u) => {
                  const ocupada = isOcupada(u.unidadID);
                  return (
                    <tr key={u.unidadID}>
                      <td className="num">{u.unidadNro}</td>
                      <td>Unidad {u.unidadNro}</td>
                      <td>
                        {ocupada ? (
                          <Badge variant="warning">Ocupada</Badge>
                        ) : (
                          <Badge variant="success">Libre</Badge>
                        )}
                      </td>
                      <td className="text-center">
                        <button
                          className="btn btn-ghost btn-sm"
                          onClick={() => setDeleteTarget({ id: u.unidadID, nro: u.unidadNro })}
                          disabled={ocupada}
                          title={ocupada ? 'No se puede eliminar una unidad ocupada' : 'Eliminar unidad'}
                        >
                          <Trash2 size={14} />
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
            </div>
            <div className="table-footer">
              <span className="text-muted">
                {lista.length} unidades &middot; <span className="num">{libres}</span> libres &middot; <span className="num">{ocupadas}</span> ocupadas
              </span>
            </div>
          </>
        )}
      </div>

      <ConfirmDialog
        open={!!deleteTarget}
        title="Eliminar unidad"
        message={`Confirme que desea eliminar la Unidad ${deleteTarget?.nro}. Esta accion no se puede deshacer.`}
        confirmLabel="Eliminar"
        variant="danger"
        onConfirm={handleDelete}
        onCancel={() => setDeleteTarget(null)}
      />
    </div>
  );
}
