export {};

declare global {
  interface Window {
    api?: {
      backendUrl: string;
      health: { check: () => Promise<{ status: string }> };
      tratamientos: Record<string, any>;
      asistencia: Record<string, any>;
      catalogos: Record<string, any>;
      dashboard: Record<string, any>;
      unidades: Record<string, any>;
      reportes: Record<string, any>;
      shell?: { openPath: (filePath: string) => Promise<string> };
    };
  }
}
