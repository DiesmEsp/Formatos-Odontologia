import { useState } from 'react';
import type { ReactNode } from 'react';
import { ChevronDown, ChevronRight } from 'lucide-react';
import { api } from '../api';
import { Badge } from './Badge';
import { formatMonto } from '../lib/format';
import type { AvanceDetalle, TratamientoAvance } from '../api/types';

function badgeVariantAvance(estado: string): 'danger' | 'success' | 'info' {
  if (estado === 'ANULADO') return 'danger';
  if (estado === 'TERMINADO') return 'success';
  return 'info';
}

export function AvanceExpandible({ avance, acciones }: { avance: TratamientoAvance; acciones?: ReactNode }) {
  const [open, setOpen] = useState(false);
  const [detalle, setDetalle] = useState<AvanceDetalle | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const toggle = async () => {
    const next = !open;
    setOpen(next);
    if (next && !detalle && !loading) {
      setLoading(true);
      setError(null);
      try {
        setDetalle(await api.tratamientos.avanceDetalle(avance.avanceID));
      } catch {
        setError('No se pudo cargar el detalle del avance.');
      } finally {
        setLoading(false);
      }
    }
  };

  return (
    <li className="material-list-item" style={{ flexWrap: 'wrap' }}>
      <button
        type="button"
        className="btn btn-ghost btn-sm"
        onClick={toggle}
        title={open ? 'Ocultar detalle' : 'Ver detalle'}
        style={{ padding: 0 }}
      >
        {open ? <ChevronDown size={14} /> : <ChevronRight size={14} />}
      </button>
      <span className="material-list-name">Avance #{avance.numero} - {avance.fecha}</span>
      <Badge variant={badgeVariantAvance(avance.estado)}>{avance.estado}</Badge>
      {acciones}
      {open && (
        <div style={{ width: '100%', paddingLeft: 22 }}>
          {loading && <span className="text-muted text-sm">Cargando detalle...</span>}
          {error && <span className="text-danger text-sm">{error}</span>}
          {detalle && (
            <>
              <div>
                <span style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-secondary)' }}>Materiales</span>
                {detalle.materiales.length === 0 ? (
                  <div className="text-muted text-sm">Sin materiales registrados</div>
                ) : (
                  <ul className="material-list">
                    {detalle.materiales.map((m) => (
                      <li key={m.materialID} className="material-list-item">
                        <span className="material-list-name">{m.nombreMaterial}</span>
                        <span className="material-list-cant">{m.cantidad} {m.unidad}</span>
                      </li>
                    ))}
                  </ul>
                )}
              </div>
              <div>
                <span style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-secondary)' }}>Pagos</span>
                {detalle.pagos.length === 0 ? (
                  <div className="text-muted text-sm">Sin pagos vinculados</div>
                ) : (
                  <ul className="material-list">
                    {detalle.pagos.map((p) => (
                      <li key={p.pagoID} className="material-list-item">
                        <span className="material-list-name">{p.fecha}</span>
                        <span className="material-list-cant">{formatMonto(p.monto)}</span>
                      </li>
                    ))}
                  </ul>
                )}
              </div>
            </>
          )}
        </div>
      )}
    </li>
  );
}
