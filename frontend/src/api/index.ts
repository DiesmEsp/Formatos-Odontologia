import type {
  Asistencia,
  AsistenciaHoy,
  CrearAsistenciaDTO,
  CrearConversionDTO,
  CrearDocenteDTO,
  CrearMaterialDTO,
  CrearOperadorDTO,
  CrearPacienteDTO,
  CrearTratamientoDTO,
  CrearTratamientoPredDTO,
  DashboardKpis,
  Docente,
  EditarTratamientoDTO,
  IngresoMensual,
  MaterialCantidadDTO,
  Materiales,
  MaterialesRegistrarDTO,
  Operador,
  Paciente,
  ReporteAnualGenerado,
  ReporteGenerado,
  ReporteReciente,
  TopMaterial,
  Tratamiento,
  TratamientoEstado,
  TratamientoMaterialConNombre,
  TratamientoPredefinido,
  TratamientoPredefinidoMaterial,
  Unidad,
  UnidadConversion,
} from './types';
import { API_BASE } from '../lib/constants';

class ApiError extends Error {
  status: number;
  constructor(message: string, status: number) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }
}

async function request<T>(endpoint: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${endpoint}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
  if (!res.ok) {
    const body = await res.text();
    let message = body;
    try {
      message = JSON.parse(body).error || body;
    } catch {}
    throw new ApiError(message, res.status);
  }
  if (res.status === 204) return undefined as T;
  return res.json();
}

export const api = {
  health: {
    check: () => request<{ status: string }>('/health'),
  },

  unidades: {
    listar: () => request<Unidad[]>('/api/unidades'),
    crear: () => request<{ id: number }>('/api/unidades', { method: 'POST' }),
    eliminar: (id: number) => request<void>(`/api/unidades/${id}`, { method: 'DELETE' }),
  },

  tratamientos: {
    activos: () => request<Tratamiento[]>('/api/tratamientos'),
    porUnidad: (unidadId: number) => request<Tratamiento[]>(`/api/tratamientos/unidad/${unidadId}`),
    buscarPorId: (id: number) => request<Tratamiento>(`/api/tratamientos/${id}`),
    crear: (data: CrearTratamientoDTO) => request<{ id: number }>('/api/tratamientos', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
    cerrar: (id: number) => request<{ ok: boolean }>(`/api/tratamientos/${id}/cerrar`, { method: 'POST' }),
    anular: (id: number, motivo: string) => request<{ ok: boolean }>(`/api/tratamientos/${id}/anular`, {
      method: 'POST',
      body: JSON.stringify({ motivo }),
    }),
    reabrir: (id: number) => request<{ ok: boolean }>(`/api/tratamientos/${id}/reabrir`, { method: 'POST' }),
    registrarPago: (id: number, abono: number) => request<{ ok: boolean }>(`/api/tratamientos/${id}/pago`, {
      method: 'POST',
      body: JSON.stringify({ abono }),
    }),
    cambiarTipo: (id: number, tipo: string) => request<{ ok: boolean }>(`/api/tratamientos/${id}/cambiar-tipo`, {
      method: 'POST',
      body: JSON.stringify({ tipo }),
    }),
    editarRetroactivo: (id: number, dto: EditarTratamientoDTO) => request<{ ok: boolean }>(`/api/tratamientos/${id}/editar`, {
      method: 'PUT',
      body: JSON.stringify(dto),
    }),
    materialesConNombre: (id: number) => request<TratamientoMaterialConNombre[]>(`/api/tratamientos/${id}/materiales`),
    agregarMaterial: (id: number, dto: MaterialCantidadDTO) => request<{ ok: boolean }>(`/api/tratamientos/${id}/materiales`, {
      method: 'POST',
      body: JSON.stringify(dto),
    }),
    quitarMaterial: (materialesListId: number) => request<void>(`/api/tratamientos/materiales/${materialesListId}`, {
      method: 'DELETE',
    }),
    actualizarCantidad: (materialesListId: number, cantidad: number) => request<{ ok: boolean }>(`/api/tratamientos/materiales/${materialesListId}`, {
      method: 'PUT',
      body: JSON.stringify({ cantidad }),
    }),
  },

  asistencia: {
    abrirDia: (dto: CrearAsistenciaDTO) => request<Asistencia>('/api/asistencia/abrir', {
      method: 'POST',
      body: JSON.stringify(dto),
    }),
    registrarMateriales: (dto: MaterialesRegistrarDTO) => request<{ ok: boolean }>('/api/asistencia/materiales', {
      method: 'POST',
      body: JSON.stringify(dto),
    }),
    acumularMaterial: (asistenciaId: number, dto: MaterialCantidadDTO) => request<{ ok: boolean }>(`/api/asistencia/${asistenciaId}/materiales`, {
      method: 'POST',
      body: JSON.stringify(dto),
    }),
    anular: (asistenciaId: number, motivo: string) => request<{ ok: boolean }>(`/api/asistencia/${asistenciaId}/anular`, {
      method: 'POST',
      body: JSON.stringify({ motivo }),
    }),
    materialesDelDia: (asistenciaId: number) => request<TratamientoMaterialConNombre[]>(`/api/asistencia/${asistenciaId}/materiales`),
  },

  catalogos: {
    operadores: {
      listar: (filtro?: string) => request<Operador[]>(`/api/catalogos/operadores${filtro ? `?q=${encodeURIComponent(filtro)}` : ''}`),
      crear: (data: CrearOperadorDTO) => request<{ id: number }>('/api/catalogos/operadores', { method: 'POST', body: JSON.stringify(data) }),
      actualizar: (data: Operador) => request<{ ok: boolean }>(`/api/catalogos/operadores/${data.operadorID}`, { method: 'PUT', body: JSON.stringify(data) }),
      eliminar: (id: number) => request<void>(`/api/catalogos/operadores/${id}`, { method: 'DELETE' }),
    },
    docentes: {
      listar: (filtro?: string) => request<Docente[]>(`/api/catalogos/docentes${filtro ? `?q=${encodeURIComponent(filtro)}` : ''}`),
      crear: (data: CrearDocenteDTO) => request<{ id: number }>('/api/catalogos/docentes', { method: 'POST', body: JSON.stringify(data) }),
      actualizar: (data: Docente) => request<{ ok: boolean }>(`/api/catalogos/docentes/${data.docenteID}`, { method: 'PUT', body: JSON.stringify(data) }),
      eliminar: (id: number) => request<void>(`/api/catalogos/docentes/${id}`, { method: 'DELETE' }),
    },
    pacientes: {
      listar: (filtro?: string) => request<Paciente[]>(`/api/catalogos/pacientes${filtro ? `?q=${encodeURIComponent(filtro)}` : ''}`),
      crear: (data: CrearPacienteDTO) => request<{ id: number }>('/api/catalogos/pacientes', { method: 'POST', body: JSON.stringify(data) }),
      actualizar: (data: Paciente) => request<{ ok: boolean }>(`/api/catalogos/pacientes/${data.pacienteID}`, { method: 'PUT', body: JSON.stringify(data) }),
      eliminar: (id: number) => request<void>(`/api/catalogos/pacientes/${id}`, { method: 'DELETE' }),
    },
    materiales: {
      listar: (filtro?: string) => request<Materiales[]>(`/api/catalogos/materiales${filtro ? `?q=${encodeURIComponent(filtro)}` : ''}`),
      buscarPorId: (id: number) => request<Materiales>(`/api/catalogos/materiales/${id}`),
      crear: (data: CrearMaterialDTO) => request<{ id: number }>('/api/catalogos/materiales', { method: 'POST', body: JSON.stringify(data) }),
      actualizar: (data: Materiales) => request<{ ok: boolean }>(`/api/catalogos/materiales/${data.materialID}`, { method: 'PUT', body: JSON.stringify(data) }),
      eliminar: (id: number) => request<void>(`/api/catalogos/materiales/${id}`, { method: 'DELETE' }),
    },
    tratamientosPred: {
      listar: (filtro?: string) => request<TratamientoPredefinido[]>(`/api/catalogos/tratamientos-pred${filtro ? `?q=${encodeURIComponent(filtro)}` : ''}`),
      crear: (data: CrearTratamientoPredDTO) => request<{ id: number }>('/api/catalogos/tratamientos-pred', { method: 'POST', body: JSON.stringify(data) }),
      actualizar: (data: TratamientoPredefinido) => request<{ ok: boolean }>(`/api/catalogos/tratamientos-pred/${data.tratPredID}`, { method: 'PUT', body: JSON.stringify(data) }),
      eliminar: (id: number) => request<void>(`/api/catalogos/tratamientos-pred/${id}`, { method: 'DELETE' }),
      materiales: (id: number) => request<TratamientoPredefinidoMaterial[]>(`/api/catalogos/tratamientos-pred/${id}/materiales`),
      guardarMateriales: (id: number, materiales: { materialID: number; cantidad: number }[]) => request<{ ok: boolean }>(`/api/catalogos/tratamientos-pred/${id}/materiales`, {
        method: 'PUT',
        body: JSON.stringify({ materiales }),
      }),
    },
    conversiones: {
      listar: () => request<UnidadConversion[]>('/api/catalogos/conversiones'),
      buscarPorMaterial: (materialId: number) => request<UnidadConversion[]>(`/api/catalogos/conversiones/material/${materialId}`),
      crear: (data: CrearConversionDTO) => request<{ id: number }>('/api/catalogos/conversiones', { method: 'POST', body: JSON.stringify(data) }),
      actualizar: (data: UnidadConversion) => request<{ ok: boolean }>(`/api/catalogos/conversiones/${data.conversionID}`, { method: 'PUT', body: JSON.stringify(data) }),
      eliminar: (id: number) => request<void>(`/api/catalogos/conversiones/${id}`, { method: 'DELETE' }),
    },
  },

  dashboard: {
    kpis: () => request<DashboardKpis>('/api/dashboard/kpis'),
    ingresosMensuales: () => request<IngresoMensual[]>('/api/dashboard/ingresos-mensuales'),
    tratamientosEstado: () => request<TratamientoEstado[]>('/api/dashboard/tratamientos-estado'),
    topMateriales: () => request<TopMaterial[]>('/api/dashboard/top-materiales'),
    asistenciaHoy: () => request<AsistenciaHoy[]>('/api/dashboard/asistencia-hoy'),
  },

  reportes: {
    generarMateriales: (anio: number, mes: number) => request<ReporteGenerado>('/api/reportes/materiales/generar', {
      method: 'POST',
      body: JSON.stringify({ anio, mes }),
    }),
    generarEconomico: (anio: number, mes: number) => request<ReporteGenerado>('/api/reportes/economico/generar', {
      method: 'POST',
      body: JSON.stringify({ anio, mes }),
    }),
    generarDocente: (anio: number, mes: number) => request<ReporteGenerado>('/api/reportes/docente/generar', {
      method: 'POST',
      body: JSON.stringify({ anio, mes }),
    }),
    generarEspecialista: (anio: number, mes: number) => request<ReporteGenerado>('/api/reportes/especialista/generar', {
      method: 'POST',
      body: JSON.stringify({ anio, mes }),
    }),
    generarAnual: (anio: number) => request<ReporteAnualGenerado>('/api/reportes/anual/generar', {
      method: 'POST',
      body: JSON.stringify({ anio }),
    }),
    listarRecientes: () => request<ReporteReciente[]>('/api/reportes/recientes'),
  },
};

export { ApiError };
export type ApiClient = typeof api;
