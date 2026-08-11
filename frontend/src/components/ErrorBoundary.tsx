import { Component, type ReactNode } from 'react';

interface ErrorBoundaryProps {
  children: ReactNode;
}

interface ErrorBoundaryState {
  hasError: boolean;
  error: Error | null;
}

export class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  constructor(props: ErrorBoundaryProps) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return { hasError: true, error };
  }

  render() {
    if (this.state.hasError) {
      return (
        <div style={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          height: '100vh',
          gap: 16,
          padding: 32,
          backgroundColor: 'var(--color-bg)',
          color: 'var(--color-text)',
        }}>
          <div style={{ fontSize: 48, fontWeight: 700, color: 'var(--color-danger-text)' }}>!</div>
          <h2 style={{ margin: 0 }}>Ha ocurrido un error inesperado</h2>
          <p className="text-muted" style={{ maxWidth: 480, textAlign: 'center' }}>
            {this.state.error?.message || 'La aplicación encontró un problema y no puede continuar.'}
          </p>
          <button
            className="btn btn-primary"
            onClick={() => {
              this.setState({ hasError: false, error: null });
              window.location.hash = '#/';
              window.location.reload();
            }}
          >
            Recargar aplicación
          </button>
        </div>
      );
    }
    return this.props.children;
  }
}
