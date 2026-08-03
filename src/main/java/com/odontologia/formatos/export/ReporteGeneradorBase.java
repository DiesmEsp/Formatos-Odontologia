package com.odontologia.formatos.export;

import com.odontologia.formatos.service.ReporteService;
import com.odontologia.formatos.util.ExportErrorUtil;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
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

    protected abstract String nombreArchivo(int anio, int mes);

    protected abstract void construir(Workbook libro, int anio, int mes) throws SQLException;

    protected Sheet crearHoja(Workbook libro, String nombre) {
        return ExcelExporter.crearHoja(libro, nombre);
    }

    protected void encabezado(Sheet hoja, List<String> encabezados) {
        ExcelExporter.escribirEncabezado(hoja, 0, encabezados);
    }

    protected void fila(Sheet hoja, int filaIndex, List<?> valores) {
        ExcelExporter.escribirFila(hoja, filaIndex, valores);
    }

    protected void autoAjustar(Sheet hoja, int numColumnas) {
        ExcelExporter.autoAjustar(hoja, numColumnas);
    }

    protected List<String> mesesAnio() {
        return Arrays.asList(
                "Ene", "Feb", "Mar", "Abr", "May", "Jun",
                "Jul", "Ago", "Sep", "Oct", "Nov", "Dic");
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
