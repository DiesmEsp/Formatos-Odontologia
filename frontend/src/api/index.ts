import type {
  Asistencia,
  AsistenciaDetalle,
  AsistenciaHoy,
  Clinica,
  CrearAsistenciaDTO,
  CrearClinicaDTO,
  CrearConversionDTO,
  CrearDocenteDTO,
  CrearMaterialDTO,
  CrearOperadorDTO,
  CrearPacienteDTO,
  CrearTratamientoDTO,
  CrearTratamientoPredDTO,
  ConsolidadoTratamiento,
  DashboardKpis,
  Docente,
  EditarTratamientoDTO,
  IngresoMensual,
  MaterialCantidadDTO,
  Materiales,
  MaterialesRegistrarDTO,
  Operador,
  Paciente,
  Pago,
  PeriodoAusencia,
  ReporteGenerado,
  ReporteReciente,
  TopMaterial,
  Tratamiento,
  TratamientoAvance,
  TratamientoEstado,
  TratamientoMaterialConNombre,
  TratamientoPredefinido,
  TratamientoPredefinidoMaterialConNombre,
  Unidad,
  UnidadConversion,
} from './types';
import { API_BASE } from '../lib/constants';
import { nombreClinicaSesion } from '../lib/clinicaStore';

class ApiError extends Error {
  status: number;
  constructor(message: string, status: number) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }
}

async function request<T>(endpoint: string, options?: RequestInit): Promise<T> {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), 15000);

  const headers = new Headers(options?.headers);
  headers.set('Content-Type', 'application/json');
  const clinicaNombre = nombreClinicaSesion();
  if (clinicaNombre) {
    headers.set('X-Clinica-Nombre', encodeURIComponent(clinicaNombre));
  }

  try {
    const res = await fetch(`${API_BASE}${endpoint}`, {
      headers,
      signal: controller.signal,
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
  } catch (err) {
    if (err instanceof DOMException && err.name === 'AbortError') {
      throw new ApiError('La solicitud ha excedido el tiempo de espera (15s).', 408);
    }
    throw err;
  } finally {
    clearTimeout(timeoutId);
  }
}

export const api = {
  health: {
    check: () => request<{ status: string }>('/health'),
  },

  clinicas: {
    listar: () => request<Clinica[]>('/api/clinicas'),
    crear: (data: CrearClinicaDTO) => request<{ id: number }>('/api/clinicas', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
    actualizar: (data: Clinica) => request<{ ok: boolean }>(`/api/clinicas/${data.clinicaID}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    }),
    eliminar: (id: number) => request<void>(`/api/clinicas/${id}`, { method: 'DELETE' }),
  },

  unidades: {
    listar: () => request<Unidad[]>('/api/unidades'),
    crear: () => request<{ id: number }>('/api/unidades', { method: 'POST' }),
    eliminar: (id: number) => request<void>(`/api/unidades/${id}`, { method: 'DELETE' }),
  },

  tratamientos: {
    activos: () => request<Tratamiento[]>('/api/tratamientos'),
    cerrados: () => request<Tratamiento[]>('/api/tratamientos/cerrados'),
    porUnidad: (unidadId: number) => request<Tratamiento[]>(`/api/tratamientos/unidad/${unidadId}`),
    buscarPorId: (id: number) => request<Tratamiento>(`/api/tratamientos/${id}`),
    crear: (data: CrearTratamientoDTO) => request<{ id: number }>('/api/tratamientos', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
    registrarCerrado: (data: {
      operadorID: number;
      pacienteID: number;
      fecha: string;
      tratPredID: number | null;
      monto: number | null;
      tipo: string;
      materiales: Record<string, number>;
    }) => request<{ id: number }>('/api/tratamientos/cerrado', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
    agregarAvance: (id: number, data: {
      fecha: string;
      unidadID?: number | null;
      pago?: number | null;
      materiales?: Record<string, number>;
    }) => request<{ id: number }>(`/api/tratamientos/${id}/avances`, {
      method: 'POST',
      body: JSON.stringify(data),
    }),
    cerrar: (id: number) => request<{ ok: boolean }>(`/api/tratamientos/${id}/cerrar`, { method: 'POST' }),
    anular: (id: number, motivo: string) => request<{ ok: boolean }>(`/api/tratamientos/${id}/anular`, {
      method: 'POST',
      body: JSON.stringify({ motivo }),
    }),
    reabrir: (id: number) => request<{ ok: boolean }>(`/api/tratamientos/${id}/reabrir`, { method: 'POST' }),
    registrarPago: (id: number, abono: number, fecha?: string) => request<{ ok: boolean }>(`/api/tratamientos/${id}/pago`, {
      method: 'POST',
      body: JSON.stringify({ abono, fecha }),
    }),
    pagos: (id: number) => request<Pago[]>(`/api/tratamientos/${id}/pagos`),
    avances: (id: number) => request<TratamientoAvance[]>(`/api/tratamientos/${id}/avances`),
    anularAvance: (avanceID: number, motivo: string) => request<{ ok: boolean }>(`/api/tratamientos/avances/${avanceID}/anular`, {
      method: 'POST',
      body: JSON.stringify({ motivo }),
    }),
    consolidado: (id: number) => request<ConsolidadoTratamiento>(`/api/tratamientos/${id}/consolidado`),
    cerradosPorPagar: () => request<Tratamiento[]>('/api/tratamientos/cerrados-por-pagar'),
    editarPago: (pagoID: number, monto: number, fecha?: string) => request<{ ok: boolean }>(`/api/tratamientos/pagos/${pagoID}`, {
      method: 'PUT',
      body: JSON.stringify({ monto, fecha }),
    }),
    eliminarPago: (pagoID: number) => request<void>(`/api/tratamientos/pagos/${pagoID}`, {
      method: 'DELETE',
    }),
    editarEnCurso: (id: number, dto: { monto?: number | null; fecha?: string; nombreTratamiento?: string; operadorID?: number | null; pacienteID?: number | null }) =>
      request<{ ok: boolean }>(`/api/tratamientos/${id}`, {
        method: 'PUT',
        body: JSON.stringify(dto),
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
    registrarEntrada: (asistenciaId: number, horaEntrada: string) => request<{ ok: boolean }>(`/api/asistencia/${asistenciaId}/entrada`, {
      method: 'PUT',
      body: JSON.stringify({ horaEntrada }),
    }),
    registrarSalida: (asistenciaId: number, horaSalida: string) => request<{ ok: boolean }>(`/api/asistencia/${asistenciaId}/salida`, {
      method: 'PUT',
      body: JSON.stringify({ horaSalida }),
    }),
    revertirSalida: (asistenciaId: number) => request<{ ok: boolean }>(`/api/asistencia/${asistenciaId}/salida`, {
      method: 'DELETE',
    }),
    porFecha: (fecha: string) => request<AsistenciaHoy[]>(`/api/asistencia/por-fecha?fecha=${encodeURIComponent(fecha)}`),
    iniciarAusencia: (asistenciaId: number, horaInicio: string, motivo?: string) => request<PeriodoAusencia>(`/api/asistencia/${asistenciaId}/ausencias`, {
      method: 'POST',
      body: JSON.stringify({ horaInicio, motivo }),
    }),
    finalizarAusencia: (asistenciaId: number, ausenciaId: number, horaFin: string) => request<{ ok: boolean }>(`/api/asistencia/${asistenciaId}/ausencias/${ausenciaId}/regresar`, {
      method: 'PUT',
      body: JSON.stringify({ horaFin }),
    }),
    eliminarAusencia: (asistenciaId: number, ausenciaId: number) => request<{ ok: boolean }>(`/api/asistencia/${asistenciaId}/ausencias/${ausenciaId}`, {
      method: 'DELETE',
    }),
    detalle: (asistenciaId: number) => request<AsistenciaDetalle>(`/api/asistencia/${asistenciaId}/detalle`),
    registrarMateriales: (dto: MaterialesRegistrarDTO) => request<{ ok: boolean }>('/api/asistencia/materiales', {
      method: 'POST',
      body: JSON.stringify(dto),
    }),
    acumularMaterial: (asistenciaId: number, dto: MaterialCantidadDTO) => request<{ ok: boolean }>(`/api/asistencia/${asistenciaId}/materiales`, {
      method: 'POST',
      body: JSON.stringify(dto),
    }),
    reemplazarMateriales: (asistenciaId: number, materiales: Record<number, number>) => request<{ ok: boolean }>(`/api/asistencia/${asistenciaId}/materiales`, {
      method: 'PUT',
      body: JSON.stringify({ materiales }),
    }),
    anular: (asistenciaId: number, motivo: string) => request<{ ok: boolean }>(`/api/asistencia/${asistenciaId}/anular`, {
      method: 'POST',
      body: JSON.stringify({ motivo }),
    }),
    materialesDelDia: (asistenciaId: number) => request<TratamientoMaterialConNombre[]>(`/api/asistencia/${asistenciaId}/materiales`),
    materialesDefault: {
      listar: () => request<{ materialId: number; cantidad: number }[]>('/api/asistencia/materiales-default'),
      guardar: (materiales: { materialId: number; cantidad: number }[]) => request<{ ok: boolean }>('/api/asistencia/materiales-default', {
        method: 'PUT',
        body: JSON.stringify({ materiales }),
      }),
    },
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
      materiales: (id: number) => request<TratamientoPredefinidoMaterialConNombre[]>(`/api/catalogos/tratamientos-pred/${id}/materiales`),
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
    generarTratamiento: (anio: number, mes: number, operadorID?: number | null, tipo?: string) => request<ReporteGenerado>('/api/reportes/tratamiento/generar', {
      method: 'POST',
      body: JSON.stringify({ anio, mes, operadorID, tipo }),
    }),
    generarAnual: (anio: number) => request<ReporteGenerado>('/api/reportes/anual/generar', {
      method: 'POST',
      body: JSON.stringify({ anio }),
    }),
    listarRecientes: () => request<ReporteReciente[]>('/api/reportes/recientes'),
    generarAsistencia: (anio: number, mes: number) => request<ReporteGenerado>('/api/reportes/asistencia/generar', {
      method: 'POST',
      body: JSON.stringify({ anio, mes }),
    }),
    generarSemilla: () => request<Record<string, string>>('/api/reportes/semilla/generar', {
      method: 'POST',
    }),
    generarConsolidado: (anio: number, mesInicio: number, mesFin: number) => request<ReporteGenerado>('/api/reportes/consolidado/generar', {
      method: 'POST',
      body: JSON.stringify({ anio, mesInicio, mesFin }),
    }),
  },
};

export { ApiError };
export type ApiClient = typeof api;
