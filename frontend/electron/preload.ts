import { contextBridge, ipcRenderer } from 'electron';

const API_BASE = 'http://localhost:7070';

async function apiFetch<T>(endpoint: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${endpoint}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
  if (!res.ok) {
    const error = await res.text();
    throw new Error(error || `HTTP ${res.status}`);
  }
  if (res.status === 204) return undefined as T;
  return res.json();
}

const electronAPI = {
  backendUrl: API_BASE,

  health: {
    check: () => apiFetch<{ status: string }>('/health'),
  },

  tratamientos: {
    activos: () => apiFetch<any[]>('/api/tratamientos'),
    porUnidad: (unidadId: number) => apiFetch<any[]>(`/api/tratamientos/unidad/${unidadId}`),
    crear: (data: any) => apiFetch<any>('/api/tratamientos', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
    buscarPorId: (id: number) => apiFetch<any>(`/api/tratamientos/${id}`),
    cerrar: (id: number) => apiFetch<void>(`/api/tratamientos/${id}/cerrar`, { method: 'POST' }),
    anular: (id: number, motivo: string) => apiFetch<void>(`/api/tratamientos/${id}/anular`, {
      method: 'POST',
      body: JSON.stringify({ motivo }),
    }),
    reabrir: (id: number) => apiFetch<void>(`/api/tratamientos/${id}/reabrir`, { method: 'POST' }),
    agregarMaterial: (id: number, materialId: number, cantidad: number) => apiFetch<void>(`/api/tratamientos/${id}/materiales`, {
      method: 'POST',
      body: JSON.stringify({ materialId, cantidad }),
    }),
    quitarMaterial: (materialesListId: number) => apiFetch<void>(`/api/tratamientos/materiales/${materialesListId}`, {
      method: 'DELETE',
    }),
    actualizarCantidad: (materialesListId: number, cantidad: number) => apiFetch<void>(`/api/tratamientos/materiales/${materialesListId}`, {
      method: 'PUT',
      body: JSON.stringify({ cantidad }),
    }),
    materialesConNombre: (id: number) => apiFetch<any[]>(`/api/tratamientos/${id}/materiales`),
    registrarPago: (id: number, abono: number) => apiFetch<void>(`/api/tratamientos/${id}/pago`, {
      method: 'POST',
      body: JSON.stringify({ abono }),
    }),
    editarRetroactivo: (id: number, dto: any) => apiFetch<void>(`/api/tratamientos/${id}/editar`, {
      method: 'PUT',
      body: JSON.stringify(dto),
    }),
    cambiarTipo: (id: number, tipo: string) => apiFetch<void>(`/api/tratamientos/${id}/cambiar-tipo`, {
      method: 'POST',
      body: JSON.stringify({ tipo }),
    }),
  },

  asistencia: {
    abrirDia: (docenteId: number, fecha: string) => apiFetch<any>('/api/asistencia/abrir', {
      method: 'POST',
      body: JSON.stringify({ docenteId, fecha }),
    }),
    registrarMateriales: (docenteId: number, fecha: string, materiales: Record<number, number>) => apiFetch<void>('/api/asistencia/materiales', {
      method: 'POST',
      body: JSON.stringify({ docenteId, fecha, materiales }),
    }),
    anular: (asistenciaId: number, motivo: string) => apiFetch<void>(`/api/asistencia/${asistenciaId}/anular`, {
      method: 'POST',
      body: JSON.stringify({ motivo }),
    }),
    acumularMaterial: (asistenciaId: number, materialId: number, cantidad: number) => apiFetch<void>(`/api/asistencia/${asistenciaId}/materiales`, {
      method: 'POST',
      body: JSON.stringify({ materialId, cantidad }),
    }),
    materialesDelDia: (asistenciaId: number) => apiFetch<any[]>(`/api/asistencia/${asistenciaId}/materiales`),
    materialesDefault: {
      listar: () => apiFetch<any[]>('/api/asistencia/materiales-default'),
      guardar: (materiales: any[]) => apiFetch<void>('/api/asistencia/materiales-default', {
        method: 'PUT',
        body: JSON.stringify({ materiales }),
      }),
    },
  },

  catalogos: {
    operadores: {
      listar: (filtro?: string) => apiFetch<any[]>(`/api/catalogos/operadores${filtro ? `?q=${encodeURIComponent(filtro)}` : ''}`),
      crear: (data: any) => apiFetch<any>('/api/catalogos/operadores', { method: 'POST', body: JSON.stringify(data) }),
      actualizar: (data: any) => apiFetch<void>(`/api/catalogos/operadores/${data.id}`, { method: 'PUT', body: JSON.stringify(data) }),
      eliminar: (id: number) => apiFetch<void>(`/api/catalogos/operadores/${id}`, { method: 'DELETE' }),
    },
    docentes: {
      listar: (filtro?: string) => apiFetch<any[]>(`/api/catalogos/docentes${filtro ? `?q=${encodeURIComponent(filtro)}` : ''}`),
      crear: (data: any) => apiFetch<any>('/api/catalogos/docentes', { method: 'POST', body: JSON.stringify(data) }),
      actualizar: (data: any) => apiFetch<void>(`/api/catalogos/docentes/${data.id}`, { method: 'PUT', body: JSON.stringify(data) }),
      eliminar: (id: number) => apiFetch<void>(`/api/catalogos/docentes/${id}`, { method: 'DELETE' }),
    },
    pacientes: {
      listar: (filtro?: string) => apiFetch<any[]>(`/api/catalogos/pacientes${filtro ? `?q=${encodeURIComponent(filtro)}` : ''}`),
      crear: (data: any) => apiFetch<any>('/api/catalogos/pacientes', { method: 'POST', body: JSON.stringify(data) }),
      actualizar: (data: any) => apiFetch<void>(`/api/catalogos/pacientes/${data.id}`, { method: 'PUT', body: JSON.stringify(data) }),
      eliminar: (id: number) => apiFetch<void>(`/api/catalogos/pacientes/${id}`, { method: 'DELETE' }),
    },
    materiales: {
      listar: (filtro?: string) => apiFetch<any[]>(`/api/catalogos/materiales${filtro ? `?q=${encodeURIComponent(filtro)}` : ''}`),
      buscarPorId: (id: number) => apiFetch<any>(`/api/catalogos/materiales/${id}`),
      crear: (data: any) => apiFetch<any>('/api/catalogos/materiales', { method: 'POST', body: JSON.stringify(data) }),
      actualizar: (data: any) => apiFetch<void>(`/api/catalogos/materiales/${data.id}`, { method: 'PUT', body: JSON.stringify(data) }),
      eliminar: (id: number) => apiFetch<void>(`/api/catalogos/materiales/${id}`, { method: 'DELETE' }),
    },
    tratamientosPred: {
      listar: (filtro?: string) => apiFetch<any[]>(`/api/catalogos/tratamientos-pred${filtro ? `?q=${encodeURIComponent(filtro)}` : ''}`),
      crear: (data: any) => apiFetch<any>('/api/catalogos/tratamientos-pred', { method: 'POST', body: JSON.stringify(data) }),
      actualizar: (data: any) => apiFetch<void>(`/api/catalogos/tratamientos-pred/${data.id}`, { method: 'PUT', body: JSON.stringify(data) }),
      eliminar: (id: number) => apiFetch<void>(`/api/catalogos/tratamientos-pred/${id}`, { method: 'DELETE' }),
      materiales: (id: number) => apiFetch<any[]>(`/api/catalogos/tratamientos-pred/${id}/materiales`),
      guardarMateriales: (id: number, materiales: any[]) => apiFetch<void>(`/api/catalogos/tratamientos-pred/${id}/materiales`, {
        method: 'PUT',
        body: JSON.stringify({ materiales }),
      }),
    },
    conversiones: {
      listar: () => apiFetch<any[]>('/api/catalogos/conversiones'),
      buscarPorMaterial: (materialId: number) => apiFetch<any[]>(`/api/catalogos/conversiones/material/${materialId}`),
      crear: (data: any) => apiFetch<any>('/api/catalogos/conversiones', { method: 'POST', body: JSON.stringify(data) }),
      actualizar: (data: any) => apiFetch<void>(`/api/catalogos/conversiones/${data.id}`, { method: 'PUT', body: JSON.stringify(data) }),
      eliminar: (id: number) => apiFetch<void>(`/api/catalogos/conversiones/${id}`, { method: 'DELETE' }),
    },
  },

  dashboard: {
    kpis: () => apiFetch<any>('/api/dashboard/kpis'),
    ingresosMensuales: () => apiFetch<any[]>('/api/dashboard/ingresos-mensuales'),
    tratamientosEstado: () => apiFetch<any[]>('/api/dashboard/tratamientos-estado'),
    topMateriales: () => apiFetch<any[]>('/api/dashboard/top-materiales'),
    asistenciaHoy: () => apiFetch<any[]>('/api/dashboard/asistencia-hoy'),
  },

  unidades: {
    listar: () => apiFetch<any[]>('/api/unidades'),
    crear: () => apiFetch<any>('/api/unidades', { method: 'POST' }),
    eliminar: (id: number) => apiFetch<void>(`/api/unidades/${id}`, { method: 'DELETE' }),
  },

  reportes: {
    generarMateriales: (anio: number, mes: number) => apiFetch<{ path: string }>('/api/reportes/materiales/generar', {
      method: 'POST',
      body: JSON.stringify({ anio, mes }),
    }),
    generarEconomico: (anio: number, mes: number) => apiFetch<{ path: string }>('/api/reportes/economico/generar', {
      method: 'POST',
      body: JSON.stringify({ anio, mes }),
    }),
    generarDocente: (anio: number, mes: number) => apiFetch<{ path: string }>('/api/reportes/docente/generar', {
      method: 'POST',
      body: JSON.stringify({ anio, mes }),
    }),
    generarEspecialista: (anio: number, mes: number) => apiFetch<{ path: string }>('/api/reportes/especialista/generar', {
      method: 'POST',
      body: JSON.stringify({ anio, mes }),
    }),
    generarAnual: (anio: number) => apiFetch<{ path: string }>('/api/reportes/anual/generar', {
      method: 'POST',
      body: JSON.stringify({ anio }),
    }),
    listarRecientes: () => apiFetch<string[]>('/api/reportes/recientes'),
    generarAsistencia: (anio: number, mes: number) => apiFetch<{ path: string }>('/api/reportes/asistencia/generar', {
      method: 'POST',
      body: JSON.stringify({ anio, mes }),
    }),
    generarSemilla: () => apiFetch<Record<string, string>>('/api/reportes/semilla/generar', {
      method: 'POST',
    }),
  },

  shell: {
    openPath: (filePath: string) => ipcRenderer.invoke('shell:openPath', filePath),
  },
};

export type ElectronAPI = typeof electronAPI;

contextBridge.exposeInMainWorld('api', electronAPI);
