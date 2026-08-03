package com.odontologia.formatos.export;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Motor de exportación a Excel con Apache POI (tarea 4.1).
 * <p>
 * Encapsula la creación del libro, hojas, encabezados con estilo,
 * ancho de columnas y el guardado del archivo.
 */
public final class ExcelExporter {

    private static final int ANCHO_MAXIMO = 60;

    private ExcelExporter() {
    }

    public static Workbook nuevoLibro() {
        return new XSSFWorkbook();
    }

    public static Sheet crearHoja(Workbook libro, String nombre) {
        return libro.createSheet(nombre);
    }

    public static void escribirEncabezado(Sheet hoja, int filaIndex, List<String> encabezados) {
        Row fila = hoja.createRow(filaIndex);
        CellStyle estilo = estiloEncabezado(hoja.getWorkbook());
        for (int i = 0; i < encabezados.size(); i++) {
            Cell celda = fila.createCell(i);
            celda.setCellValue(encabezados.get(i));
            celda.setCellStyle(estilo);
        }
    }

    public static void escribirFila(Sheet hoja, int filaIndex, List<?> valores) {
        Row fila = hoja.createRow(filaIndex);
        for (int i = 0; i < valores.size(); i++) {
            Object valor = valores.get(i);
            Cell celda = fila.createCell(i);
            if (valor instanceof Number numero) {
                celda.setCellValue(numero.doubleValue());
            } else if (valor != null) {
                celda.setCellValue(valor.toString());
            }
        }
    }

    public static void autoAjustar(Sheet hoja, int numColumnas) {
        for (int col = 0; col < numColumnas; col++) {
            int ancho = 10;
            for (int filaIdx = 0; filaIdx <= hoja.getLastRowNum(); filaIdx++) {
                Row fila = hoja.getRow(filaIdx);
                if (fila == null) {
                    continue;
                }
                Cell celda = fila.getCell(col);
                if (celda == null || celda.getCellType() == org.apache.poi.ss.usermodel.CellType.BLANK) {
                    continue;
                }
                String texto;
                switch (celda.getCellType()) {
                    case STRING -> texto = celda.getStringCellValue();
                    case NUMERIC -> texto = String.valueOf(celda.getNumericCellValue());
                    default -> texto = "";
                }
                ancho = Math.max(ancho, texto.length());
            }
            hoja.setColumnWidth(col, Math.min(ancho, ANCHO_MAXIMO) * 256);
        }
    }

    public static void guardar(Workbook libro, Path destino) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(destino.toFile())) {
            libro.write(fos);
        }
    }

    private static CellStyle estiloEncabezado(Workbook libro) {
        CellStyle estilo = libro.createCellStyle();
        estilo.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estilo.setBorderBottom(BorderStyle.THIN);
        estilo.setBorderTop(BorderStyle.THIN);
        estilo.setBorderLeft(BorderStyle.THIN);
        estilo.setBorderRight(BorderStyle.THIN);
        Font fuente = libro.createFont();
        fuente.setBold(true);
        fuente.setColor(IndexedColors.WHITE.getIndex());
        estilo.setFont(fuente);
        return estilo;
    }
}
