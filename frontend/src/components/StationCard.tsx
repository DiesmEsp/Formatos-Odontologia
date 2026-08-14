import { Badge } from './Badge';
import { formatMonto } from '../lib/format';
import { User, Stethoscope } from 'lucide-react';
import type { Tratamiento } from '../api/types';

interface StationCardProps {
  unidadNro: number;
  tratamiento: Tratamiento | null;
  operadorNombre?: string;
  pacienteNombre?: string;
  onClick: (unidadNro: number) => void;
}

export function StationCard({ unidadNro, tratamiento, operadorNombre, pacienteNombre, onClick }: StationCardProps) {
  const ocupado = !!tratamiento;

  return (
    <div
      className={`station-card ${ocupado ? 'ocupado' : 'libre'}`}
      onClick={() => onClick(unidadNro)}
    >
      <div className="station-header">
        <span className="station-num">Unidad {unidadNro}</span>
        <span className="station-status">
          <span className={`led ${ocupado ? 'led-warn' : 'led-ok'}`} />
          {ocupado ? 'En curso' : 'Libre'}
        </span>
      </div>

      {ocupado && tratamiento ? (
        <div className="station-ticket">
          <div className="ticket-head">
            <span className="ticket-nro">#{tratamiento.tratamientoID}</span>
            <span className="ticket-tipo">{tratamiento.nombreTratamiento}</span>
          </div>
          <div className="ticket-meta">
            <div className="meta-row">
              <Stethoscope size={14} />
              <span>{operadorNombre ?? `Operador #${tratamiento.operadorID}`}</span>
            </div>
            <div className="meta-row">
              <User size={14} />
              <span>{pacienteNombre ?? `Paciente #${tratamiento.pacienteID}`}</span>
            </div>
          </div>
          <div className="ticket-monto">{formatMonto(tratamiento.monto)}</div>
          <div className="ticket-actions">
            <Badge variant={tratamiento.estado === 'ABIERTO' ? 'info' : 'success'}>
              {tratamiento.estado}
            </Badge>
          </div>
        </div>
      ) : (
        <div className="station-empty">
          <button className="btn-station" onClick={(e) => { e.stopPropagation(); onClick(unidadNro); }}>
            Nuevo tratamiento
          </button>
        </div>
      )}
    </div>
  );
}
