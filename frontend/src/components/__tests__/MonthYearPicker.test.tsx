import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { MonthYearPicker } from '../MonthYearPicker';

describe('MonthYearPicker', () => {
  it('renderiza selects de mes y año', () => {
    render(
      <MonthYearPicker mes={7} anio={2026} onMesChange={vi.fn()} onAnioChange={vi.fn()} onGenerate={vi.fn()} />
    );
    const selects = document.querySelectorAll('.picker-select');
    expect(selects).toHaveLength(2);
  });

  it('muestra el boton de generar', () => {
    render(
      <MonthYearPicker mes={7} anio={2026} onMesChange={vi.fn()} onAnioChange={vi.fn()} onGenerate={vi.fn()} />
    );
    expect(screen.getByText('Generar reporte')).toBeInTheDocument();
  });

  it('llama a onGenerate al hacer clic', () => {
    const onGenerate = vi.fn();
    render(
      <MonthYearPicker mes={7} anio={2026} onMesChange={vi.fn()} onAnioChange={vi.fn()} onGenerate={onGenerate} />
    );
    fireEvent.click(screen.getByText('Generar reporte'));
    expect(onGenerate).toHaveBeenCalled();
  });

  it('muestra estado generating', () => {
    render(
      <MonthYearPicker mes={7} anio={2026} onMesChange={vi.fn()} onAnioChange={vi.fn()} onGenerate={vi.fn()} generating />
    );
    expect(screen.getByText('Generando...')).toBeInTheDocument();
  });

  it('boton esta deshabilitado cuando generating es true', () => {
    render(
      <MonthYearPicker mes={7} anio={2026} onMesChange={vi.fn()} onAnioChange={vi.fn()} onGenerate={vi.fn()} generating />
    );
    expect(screen.getByText('Generando...')).toBeDisabled();
  });

  it('llama a onMesChange al cambiar mes', () => {
    const onMesChange = vi.fn();
    render(
      <MonthYearPicker mes={7} anio={2026} onMesChange={onMesChange} onAnioChange={vi.fn()} onGenerate={vi.fn()} />
    );
    fireEvent.change(document.querySelectorAll('.picker-select')[0], { target: { value: '3' } });
    expect(onMesChange).toHaveBeenCalledWith(3);
  });

  it('llama a onAnioChange al cambiar año', () => {
    const onAnioChange = vi.fn();
    render(
      <MonthYearPicker mes={7} anio={2026} onMesChange={vi.fn()} onAnioChange={onAnioChange} onGenerate={vi.fn()} />
    );
    fireEvent.change(document.querySelectorAll('.picker-select')[1], { target: { value: '2025' } });
    expect(onAnioChange).toHaveBeenCalledWith(2025);
  });

  it('muestra boton Anual cuando showAnual es true', () => {
    render(
      <MonthYearPicker mes={7} anio={2026} onMesChange={vi.fn()} onAnioChange={vi.fn()} onGenerate={vi.fn()} showAnual onGenerateAnual={vi.fn()} />
    );
    expect(screen.getByText('Anual 2026')).toBeInTheDocument();
  });
});
