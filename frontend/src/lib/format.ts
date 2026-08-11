export function formatMonto(valor: number): string {
  return `S/ ${valor.toFixed(2)}`;
}

export function formatFecha(fecha: string): string {
  if (!fecha) return '';
  const d = new Date(fecha + 'T00:00:00');
  return d.toLocaleDateString('es-PE', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

export function formatMes(mes: number): string {
  const meses = [
    'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
    'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre',
  ];
  return meses[mes - 1] ?? '';
}

export function formatDateTime(ts: string): string {
  if (!ts) return '';
  const d = new Date(ts);
  return d.toLocaleDateString('es-PE', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  });
}

export function formatBytes(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

export function hoyISO(): string {
  const d = new Date();
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

export function mesActual(): number {
  return new Date().getMonth() + 1;
}

export function anioActual(): number {
  return new Date().getFullYear();
}

export function nombreCompleto(nombres: string, apellidos: string): string {
  return `${nombres} ${apellidos}`;
}

export function horaActual(): string {
  const d = new Date();
  const h = String(d.getHours()).padStart(2, '0');
  const m = String(d.getMinutes()).padStart(2, '0');
  const s = String(d.getSeconds()).padStart(2, '0');
  return `${h}:${m}:${s}`;
}

export function formatearHora(hhmmss: string | null): string {
  if (!hhmmss) return '';
  return hhmmss.substring(0, 5);
}

export function calcularDuracion(inicio: string | null, fin: string | null): string {
  if (!inicio || !fin) return '';
  const [h1, m1] = inicio.split(':').map(Number);
  const [h2, m2] = fin.split(':').map(Number);
  const totalMin = (h2 * 60 + m2) - (h1 * 60 + m1);
  if (totalMin <= 0) return '';
  const horas = Math.floor(totalMin / 60);
  const minutos = totalMin % 60;
  if (horas === 0) return `${minutos}m`;
  if (minutos === 0) return `${horas}h`;
  return `${horas}h ${minutos}m`;
}
