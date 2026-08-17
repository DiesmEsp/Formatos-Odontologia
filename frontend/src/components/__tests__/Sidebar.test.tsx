import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { Sidebar } from '../Sidebar';

function renderSidebar() {
  return render(
    <BrowserRouter>
      <Sidebar />
    </BrowserRouter>
  );
}

describe('Sidebar', () => {
  it('renderiza la marca', () => {
    renderSidebar();
    expect(screen.getByText('Formatos')).toBeInTheDocument();
    expect(screen.getByText('Odontologicos')).toBeInTheDocument();
  });

  it('renderiza las secciones de navegacion', () => {
    renderSidebar();
    expect(screen.getByText('Atencion')).toBeInTheDocument();
    expect(screen.getByText('Gestion')).toBeInTheDocument();
  });

  it('renderiza todos los links', () => {
    renderSidebar();
    expect(screen.getByText('Dashboard')).toBeInTheDocument();
    expect(screen.getByText('Tratamientos')).toBeInTheDocument();
    expect(screen.getByText('Asistencia')).toBeInTheDocument();
    expect(screen.getByText('Catalogos')).toBeInTheDocument();
    expect(screen.getByText('Pagos')).toBeInTheDocument();
    expect(screen.getByText('Reportes')).toBeInTheDocument();
    expect(screen.getByText('Unidades')).toBeInTheDocument();
    expect(screen.getByText('Clinicas')).toBeInTheDocument();
  });

  it('los links son NavLink con clase sidebar-link', () => {
    renderSidebar();
    const links = document.querySelectorAll('.sidebar-link');
    expect(links).toHaveLength(8);
  });

  it('el link activo tiene clase active', () => {
    window.history.pushState({}, '', '/tratamientos');
    renderSidebar();
    const activeLink = document.querySelector('.sidebar-link.active');
    expect(activeLink).toBeTruthy();
  });
});
