import { Badge } from './Badge';
import { formatMonto } from '../lib/format';
import type { Tratamiento } from '../api/types';

interface StationCardProps {
  unidadNro: number;
  tratamiento: Tratamiento | null;
  onClick: (unidadNro: number) => void;
}

export function StationCard({ unidadNro, tratamiento, onClick }: StationCardProps) {
  const ocupado = !!tratamiento;

  return (
    <div
      className={`station-card ${ocupado ? 'ocupado' : 'libre'}`}
      onClick={() => onClick(unidadNro)}
    >
      <div className="station-header">
        <span className="station-num">Unidad {unidadNro}</span>
        <span className="station-status">{ocupado ? 'Ocupada' : 'Libre'}</span>
      </div>

      {ocupado && tratamiento ? (
        <div className="station-ticket">
          <span className="ticket-nro">#{tratamiento.tratamientoID}</span>
          <span className="ticket-tipo">{tratamiento.nombreTratamiento}</span>
          <div className="ticket-meta">
            <span>{tratamiento.tipo}</span>
            <Badge variant={tratamiento.estado === 'ABIERTO' ? 'info' : 'success'}>
              {tratamiento.estado}
            </Badge>
          </div>
          <span className="ticket-monto">{formatMonto(tratamiento.monto)}</span>
        </div>
      ) : (
        <div className="station-empty">
          <span className="station-empty-text">Click para iniciar tratamiento</span>
        </div>
      )}
    </div>
  );
}
