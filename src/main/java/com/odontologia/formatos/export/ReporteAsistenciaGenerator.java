package com.odontologia.formatos.export;

import com.odontologia.formatos.repository.ReporteRepository.FilaAsistencia;
import com.odontologia.formatos.repository.ReporteRepository.FilaAusencia;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ReporteAsistenciaGenerator extends ReporteGeneradorBase {

    @Override
    protected String nombreArchivo(int anio, int mes) {
        return ReporteNomenclatura.nombreAsistencia(anio, mes);
    }

    @Override
    protected void construir(Workbook libro, int anio, int mes) throws SQLException {
        List<FilaAsistencia> datos = service.datosAsistencia(anio, mes);
        construirResumen(libro, anio, mes, datos);
        construirDetalleAusencias(libro, anio, mes, datos);
    }

    private void construirResumen(Workbook libro, int anio, int mes, List<FilaAsistencia> datos) {
        Sheet hoja = crearHoja(libro, "Resumen");
        String mesTexto = ReporteNomenclatura.mes(mes);
        titulo(hoja, "Asistencia Docente — " + mesTexto + " " + anio, 8);

        List<String> encabezados = List.of("Docente", "Fecha", "Hora Entrada", "Hora Salida",
                "Horas Totales", "Horas Ausencia", "Horas Presencia", "N° Ausencias");
        encabezado(hoja, 2, encabezados);

        int filaIdx = 3;

        for (FilaAsistencia a : datos) {
            List<Object> valores = new ArrayList<>();
            double horasTotales = calcularHoras(a.getHoraEntrada(), a.getHoraSalida());
            double horasAusencia = 0;
            for (FilaAusencia aus : a.getAusencias()) {
                horasAusencia += calcularHoras(aus.getHoraInicio(), aus.getHoraFin());
            }
            double horasPresencia = Math.max(0, horasTotales - horasAusencia);

            valores.add(a.getDocente());
            valores.add(a.getFecha());
            valores.add(a.getHoraEntrada() != null ? a.getHoraEntrada().substring(0, 5) : "");
            valores.add(a.getHoraSalida() != null ? a.getHoraSalida().substring(0, 5) : "");
            valores.add(redondear2(horasTotales));
            valores.add(redondear2(horasAusencia));
            valores.add(redondear2(horasPresencia));
            valores.add(a.getAusencias().size());

            fila(hoja, filaIdx, valores);
            filaIdx++;
        }

        autoAjustar(hoja, 8);
    }

    private void construirDetalleAusencias(Workbook libro, int anio, int mes, List<FilaAsistencia> datos) {
        Sheet hoja = crearHoja(libro, "Detalle Ausencias");
        String mesTexto = ReporteNomenclatura.mes(mes);
        titulo(hoja, "Detalle de Ausencias — " + mesTexto + " " + anio, 6);

        List<String> encabezados = List.of("Docente", "Fecha", "Hora Inicio", "Hora Fin", "Duración (h)", "Motivo");
        encabezado(hoja, 2, encabezados);

        int filaIdx = 3;

        for (FilaAsistencia a : datos) {
            for (FilaAusencia aus : a.getAusencias()) {
                List<Object> valores = new ArrayList<>();
                double duracion = calcularHoras(aus.getHoraInicio(), aus.getHoraFin());

                valores.add(a.getDocente());
                valores.add(a.getFecha());
                valores.add(aus.getHoraInicio() != null ? aus.getHoraInicio().substring(0, 5) : "");
                valores.add(aus.getHoraFin() != null ? aus.getHoraFin().substring(0, 5) : "");
                valores.add(redondear2(duracion));
                valores.add(aus.getMotivo() != null ? aus.getMotivo() : "");

                fila(hoja, filaIdx, valores);
                filaIdx++;
            }
        }

        if (filaIdx == 3) {
            fila(hoja, filaIdx, List.of("No se registraron periodos de ausencia en este mes."));
        }

        autoAjustar(hoja, 6);
    }

    private double calcularHoras(String inicio, String fin) {
        if (inicio == null || fin == null) return 0;
        try {
            LocalTime tInicio = LocalTime.parse(inicio.length() >= 8 ? inicio.substring(0, 8) : inicio + ":00");
            LocalTime tFin = LocalTime.parse(fin.length() >= 8 ? fin.substring(0, 8) : fin + ":00");
            return Duration.between(tInicio, tFin).toMinutes() / 60.0;
        } catch (Exception e) {
            return 0;
        }
    }
}
