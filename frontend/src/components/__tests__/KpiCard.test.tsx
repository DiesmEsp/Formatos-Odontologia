import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { KpiCard } from '../KpiCard';
import { DollarSign } from 'lucide-react';

describe('KpiCard', () => {
  it('renderiza label, value y sub', () => {
    render(
      <KpiCard label="Ingresos" value="S/ 100.00" sub="+12%" icon={DollarSign} />
    );
    expect(screen.getByText('Ingresos')).toBeInTheDocument();
    expect(screen.getByText('S/ 100.00')).toBeInTheDocument();
    expect(screen.getByText('+12%')).toBeInTheDocument();
  });

  it('no renderiza sub si no se provee', () => {
    render(<KpiCard label="Total" value="42" icon={DollarSign} />);
    expect(screen.queryByText('+12%')).not.toBeInTheDocument();
  });

  it('aplica clase de icono warning', () => {
    const { container } = render(
      <KpiCard label="X" value="0" icon={DollarSign} variant="warning" />
    );
    expect(container.querySelector('.kpi-icon-warn')).toBeTruthy();
  });

  it('aplica clase de icono success', () => {
    const { container } = render(
      <KpiCard label="X" value="0" icon={DollarSign} variant="success" />
    );
    expect(container.querySelector('.kpi-icon-ok')).toBeTruthy();
  });

  it('variant default no tiene clase especial de icono', () => {
    const { container } = render(
      <KpiCard label="X" value="0" icon={DollarSign} />
    );
    expect(container.querySelector('.kpi-icon-warn')).toBeFalsy();
    expect(container.querySelector('.kpi-icon-ok')).toBeFalsy();
  });
});
