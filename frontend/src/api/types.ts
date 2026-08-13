export interface Materiales {
  materialID: number;
  nombre: string;
  unidad: string;
  estado: number;
}

export interface Docente {
  docenteID: number;
  nombres: string;
  apellidos: string;
  telefono: string;
  estado: number;
}

export interface Operador {
  operadorID: number;
  nombres: string;
  apellidos: string;
  dni?: string;
  grado: string;
  tipo: string;
  periodo: number;
  estado: number;
}

export interface Paciente {
  pacienteID: number;
  nombres: string;
  apellidos: string;
  estado: number;
}

export interface Unidad {
  unidadID: number;
  unidadNro: number;
}

export interface TratamientoPredefinido {
  tratPredID: number;
  nombreTratamiento: string;
  montoSugerido: number | null;
  estado: number;
}

export interface TratamientoPredefinidoMaterial {
  materialListPredID: number;
  tratPredID: number;
  materialID: number;
  cantidad: number;
}

export interface TratamientoPredefinidoMaterialConNombre {
  materialID: number;
  nombreMaterial: string;
  cantidad: number;
}

export interface Tratamiento {
  tratamientoID: number;
  operadorID: number;
  pacienteID: number;
  unidadID: number | null;
  fecha: string;
  nombreTratamiento: string;
  monto: number;
  tipo: string;
  estadoPago: string;
  montoPagado: number;
  estado: string;
  cerradoEn: string | null;
}

export interface TratamientoMaterial {
  materialesListID: number;
  tratamientoID: number;
  materialID: number;
  cantidad: number;
}

export interface TratamientoMaterialConNombre {
  materialesListID: number;
  tratamientoID: number;
  materialID: number;
  nombreMaterial: string;
  cantidad: number;
}

export interface Asistencia {
  asistenciaID: number;
  docenteID: number;
  fecha: string;
  estado: string;
  horaEntrada: string | null;
  horaSalida: string | null;
}

export interface PeriodoAusencia {
  ausenciaID: number;
  asistenciaID: number;
  horaInicio: string;
  horaFin: string | null;
  motivo: string | null;
}

export interface AsistenciaDetalle {
  asistencia: Asistencia;
  ausencias: PeriodoAusencia[];
  materiales: TratamientoMaterialConNombre[];
}

export interface AsistenciaMaterial {
  matAsistenciaID: number;
  asistenciaID: number;
  materialID: number;
  cantidad: number;
}

export interface UnidadConversion {
  conversionID: number;
  materialID: number;
  unidadBase: string;
  unidadEmpaque: string;
  factor: number;
}

export interface RegistroAnulacion {
  anulacionID: number;
  tablaAfectada: string;
  idRegistroAnulado: number;
  motivo: string;
  usuario: string;
  timestamp: string;
}

export interface CrearTratamientoDTO {
  operadorID: number;
  pacienteID: number;
  unidadID: number | null;
  fecha: string;
  tratPredID: number | null;
  monto: number | null;
  tipo: string;
}

export interface EditarTratamientoDTO {
  tipo: string;
  monto: number | null;
  montoPagado: number | null;
  estadoPago: string | null;
  fecha: string;
  nombreTratamiento: string;
  operadorID: number | null;
  pacienteID: number | null;
  cantidadesMateriales: Record<string, number>;
}

export interface DashboardKpis {
  ingresosMes: number;
  ingresosSemana: number;
  tratamientosCurso: number;
  docentesHoy: number;
}

export interface IngresoMensual {
  mes: number;
  monto: number;
}

export interface TratamientoEstado {
  estado: string;
  count: number;
}

export interface TopMaterial {
  nombre: string;
  cantidad: number;
}

export interface AsistenciaHoy {
  docenteID: number;
  nombres: string;
  apellidos: string;
  presente: boolean;
  asistenciaID: number | null;
  horaEntrada: string | null;
  horaSalida: string | null;
  enAusencia: boolean;
}

export interface ReporteGenerado {
  path: string;
}

export interface ReporteAnualGenerado {
  reporteMateriales: string;
  reporteEconomico: string;
}

export interface ReporteReciente {
  nombre: string;
  path: string;
  tamano: string;
}

export interface ApiError {
  error: string;
}

export interface CrearOperadorDTO {
  nombres: string;
  apellidos: string;
  dni?: string;
  grado: string;
  tipo: string;
  periodo: number;
}

export interface CrearDocenteDTO {
  nombres: string;
  apellidos: string;
  telefono: string;
}

export interface CrearPacienteDTO {
  nombres: string;
  apellidos: string;
}

export interface CrearMaterialDTO {
  nombre: string;
  unidad: string;
}

export interface CrearTratamientoPredDTO {
  nombreTratamiento: string;
  montoSugerido: number | null;
}

export interface CrearConversionDTO {
  materialID: number;
  unidadBase: string;
  unidadEmpaque: string;
  factor: number;
}

export interface CrearAsistenciaDTO {
  docenteId: number;
  fecha: string;
  horaEntrada?: string;
}

export interface MaterialCantidadDTO {
  materialId: number;
  cantidad: number;
}

export interface MaterialesRegistrarDTO {
  docenteId: number;
  fecha: string;
  materiales: Record<string, number>;
}

export type BadgeVariant = 'success' | 'warning' | 'danger' | 'info' | 'neutral';
export type ButtonVariant = 'primary' | 'secondary' | 'danger' | 'success' | 'warning' | 'ghost';
export type ButtonSize = 'sm' | 'md' | 'lg';
export type ToastType = 'success' | 'error' | 'warning' | 'info';
export type TratamientoEstadoValue = 'ABIERTO' | 'CERRADO' | 'ANULADO';
export type TratamientoTipo = 'NORMAL' | 'CONTINUO';
export type EstadoPago = 'PENDIENTE' | 'PAGADO' | 'PARCIAL';
export type GradoOperador = 'PRE' | 'POS';
export type TipoOperadorPRE = '3' | '4' | '5';
export type TipoOperadorPOS = 'R1' | 'R2' | 'R3';
