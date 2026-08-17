package com.odontologia.formatos.export;

import com.odontologia.formatos.service.ReporteService;
import com.odontologia.formatos.util.ExportErrorUtil;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Base común para los generadores de reportes: crea el libro, delega la
 * construcción de la hoja al generador y guarda el archivo con traducción
 * de errores (ExportErrorUtil).
 */
public abstract class ReporteGeneradorBase {

    protected final ReporteService service;

    protected ReporteGeneradorBase() {
        this.service = new ReporteService();
    }

    public Path generar(int anio, int mes, Path carpetaDestino) throws SQLException, ReporteException {
        Path destino = carpetaDestino.resolve(nombreArchivo(anio, mes));
        Workbook libro = ExcelExporter.nuevoLibro();
        try {
            construir(libro, anio, mes);
            ExcelExporter.guardar(libro, destino);
        } catch (IOException e) {
            throw new ReporteException(ExportErrorUtil.mensaje(e, destino.toString()));
        } finally {
            cerrarQuietamente(libro);
        }
        return destino;
    }

    public Path generar(int anio, int mesInicio, int mesFin, Path carpetaDestino) throws SQLException, ReporteException {
        Path destino = carpetaDestino.resolve(nombreArchivoRango(anio, mesInicio, mesFin));
        Workbook libro = ExcelExporter.nuevoLibro();
        try {
            construirRango(libro, anio, mesInicio, mesFin);
            ExcelExporter.guardar(libro, destino);
        } catch (IOException e) {
            throw new ReporteException(ExportErrorUtil.mensaje(e, destino.toString()));
        } finally {
            cerrarQuietamente(libro);
        }
        return destino;
    }

    protected abstract String nombreArchivo(int anio, int mes);

    protected String nombreArchivoRango(int anio, int mesInicio, int mesFin) {
        return nombreArchivo(anio, mesInicio);
    }

    protected abstract void construir(Workbook libro, int anio, int mes) throws SQLException;

    protected void construirRango(Workbook libro, int anio, int mesInicio, int mesFin) throws SQLException {
        construir(libro, anio, mesInicio);
    }

    protected Sheet crearHoja(Workbook libro, String nombre) {
        return ExcelExporter.crearHoja(libro, nombre);
    }

    protected void encabezado(Sheet hoja, List<String> encabezados) {
        ExcelExporter.escribirEncabezado(hoja, 0, encabezados);
    }

    protected void encabezado(Sheet hoja, int filaIndex, List<String> encabezados) {
        ExcelExporter.escribirEncabezado(hoja, filaIndex, encabezados);
    }

    protected void encabezadoDia(Sheet hoja, int filaIndex, List<String> encabezados, boolean[] domingo) {
        ExcelExporter.escribirEncabezadoDia(hoja, filaIndex, encabezados, domingo);
    }

    protected void titulo(Sheet hoja, String texto, int colspan) {
        ExcelExporter.escribirTitulo(hoja, 0, texto, colspan);
    }

    protected void seccion(Sheet hoja, int filaIndex, String texto, int colspan) {
        ExcelExporter.escribirEncabezadoSeccion(hoja, filaIndex, texto, colspan);
    }

    protected void fila(Sheet hoja, int filaIndex, List<?> valores) {
        ExcelExporter.escribirFila(hoja, filaIndex, valores);
    }

    protected void filaTotal(Sheet hoja, int filaIndex, List<Object> valores, int colInicio) {
        ExcelExporter.escribirTotal(hoja, filaIndex, valores, colInicio);
    }

    protected void autoAjustar(Sheet hoja, int numColumnas) {
        ExcelExporter.autoAjustar(hoja, numColumnas);
    }

    protected List<String> mesesAnio() {
        return Arrays.asList(
                "Ene", "Feb", "Mar", "Abr", "May", "Jun",
                "Jul", "Ago", "Sep", "Oct", "Nov", "Dic");
    }

    protected List<Integer> mesesEnRango(int mesInicio, int mesFin) {
        List<Integer> meses = new ArrayList<>();
        for (int m = mesInicio; m <= mesFin; m++) {
            meses.add(m);
        }
        return meses;
    }

    protected List<String> nombresMesesEnRango(int mesInicio, int mesFin) {
        List<String> nombres = mesesAnio();
        List<String> resultado = new ArrayList<>();
        for (int m = mesInicio; m <= mesFin; m++) {
            resultado.add(nombres.get(m - 1));
        }
        return resultado;
    }

    protected int diasDelMes(int anio, int mes) {
        return ExcelExporter.diasDelMes(anio, mes);
    }

    protected String diaConNombre(int anio, int mes, int dia) {
        return dia + " " + nombreDiaCorto(anio, mes, dia);
    }

    protected boolean esDomingo(int anio, int mes, int dia) {
        return java.time.LocalDate.of(anio, mes, dia).getDayOfWeek() == java.time.DayOfWeek.SUNDAY;
    }

    private String nombreDiaCorto(int anio, int mes, int dia) {
        switch (java.time.LocalDate.of(anio, mes, dia).getDayOfWeek()) {
            case MONDAY: return "lun";
            case TUESDAY: return "mar";
            case WEDNESDAY: return "mié";
            case THURSDAY: return "jue";
            case FRIDAY: return "vie";
            case SATURDAY: return "sáb";
            default: return "dom";
        }
    }

    protected double redondear2(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    private void cerrarQuietamente(Workbook libro) {
        try {
            libro.close();
        } catch (IOException ignorada) {
            // cierre best-effort; el trabajo útil ya quedó escrito en el archivo
        }
    }
}
