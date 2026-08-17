import { useState } from 'react';
import { useApi } from '../hooks/useApi';
import { useToast } from '../hooks/useToast';
import { api } from '../api';
import { StationCard } from '../components/StationCard';
import { Plus } from 'lucide-react';
import { nombreCompleto } from '../lib/format';
import type { Tratamiento, Unidad } from '../api/types';
import { CrearTratamientoModal } from './tratamientos/CrearTratamientoModal';
import { DetalleTratamientoSubventana } from './tratamientos/DetalleTratamientoSubventana';

export default function Tratamientos() {
  const unidades = useApi(() => api.unidades.listar());
  const tratamientos = useApi(() => api.tratamientos.activos());
  const operadores = useApi(() => api.catalogos.operadores.listar());
  const pacientes = useApi(() => api.catalogos.pacientes.listar());
  const { addToast } = useToast();

  const [crearUnidad, setCrearUnidad] = useState<Unidad | null>(null);
  const [crearManual, setCrearManual] = useState(false);
  const [detalleTratamiento, setDetalleTratamiento] = useState<Tratamiento | null>(null);

  const tratamientosActivos = tratamientos.data ?? [];
  const unidadesList = unidades.data ?? [];

  const operadorNombreMap = new Map((operadores.data ?? []).map((o) => [o.operadorID, nombreCompleto(o.nombres, o.apellidos)]));
  const pacienteNombreMap = new Map((pacientes.data ?? []).map((p) => [p.pacienteID, nombreCompleto(p.nombres, p.apellidos)]));

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
        <button className="btn btn-primary" onClick={() => setCrearManual(true)}>
          <Plus size={16} /> Nuevo tratamiento (manual)
        </button>
      </div>

      <div className="station-grid">
        {unidadesList.map((u) => {
          const t = getTratamientoEnUnidad(u.unidadID);
          return (
            <StationCard
              key={u.unidadID}
              unidadNro={u.unidadNro}
              tratamiento={t}
              operadorNombre={t ? operadorNombreMap.get(t.operadorID) : undefined}
              pacienteNombre={t ? pacienteNombreMap.get(t.pacienteID) : undefined}
              onClick={() => {
                if (t) setDetalleTratamiento(t);
                else setCrearUnidad(u);
              }}
            />
          );
        })}
        {unidadesList.length === 0 && (
          <div className="empty-state full">
            <span className="empty-title">No hay unidades registradas</span>
            <span className="empty-text">Cree unidades en la sección de Gestión para comenzar.</span>
          </div>
        )}
      </div>

      {crearUnidad && (
        <CrearTratamientoModal
          unidad={crearUnidad}
          unidadesList={unidadesList}
          onClose={() => setCrearUnidad(null)}
          onSuccess={() => { setCrearUnidad(null); tratamientos.refetch(); operadores.refetch(); pacientes.refetch(); }}
          addToast={addToast}
        />
      )}

      {crearManual && (
        <CrearTratamientoModal
          unidad={null}
          unidadesList={unidadesList}
          onClose={() => setCrearManual(false)}
          onSuccess={() => { setCrearManual(false); tratamientos.refetch(); operadores.refetch(); pacientes.refetch(); }}
          addToast={addToast}
        />
      )}

      {detalleTratamiento && (
        <DetalleTratamientoSubventana
          tratamiento={detalleTratamiento}
          operadorNombre={operadorNombreMap.get(detalleTratamiento.operadorID)}
          pacienteNombre={pacienteNombreMap.get(detalleTratamiento.pacienteID)}
          onClose={() => { setDetalleTratamiento(null); tratamientos.refetch(); }}
          addToast={addToast}
        />
      )}
    </div>
  );
}
