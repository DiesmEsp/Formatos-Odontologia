import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Badge } from '../Badge';

describe('Badge', () => {
  it('renderiza el texto', () => {
    render(<Badge variant="success">Activo</Badge>);
    expect(screen.getByText('Activo')).toBeInTheDocument();
  });

  it('aplica la clase correcta segun variant', () => {
    const { container } = render(<Badge variant="success">Activo</Badge>);
    expect(container.querySelector('.badge-success')).toBeTruthy();
  });

  it('cada variant tiene su clase', () => {
    const variants = ['success', 'warning', 'danger', 'info', 'neutral'] as const;
    variants.forEach((v) => {
      const { container, unmount } = render(<Badge variant={v}>X</Badge>);
      expect(container.querySelector(`.badge-${v}`)).toBeTruthy();
      unmount();
    });
  });

  it('acepta className adicional', () => {
    const { container } = render(<Badge variant="info" className="mt-4">X</Badge>);
    expect(container.querySelector('.mt-4')).toBeTruthy();
  });
});
