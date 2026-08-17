package com.odontologia.formatos.export;

import com.odontologia.formatos.repository.ReporteRepository.FilaDocente;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reporte de consumo de materiales por docente.
 * 1 hoja con tablas por docente y desglose diario.
 */
public class ReporteDocenteGenerator extends ReporteGeneradorBase {

    @Override
    protected String nombreArchivo(int anio, int mes) {
        return ReporteNomenclatura.nombreDocente(anio, mes);
    }

    @Override
    protected String nombreArchivoRango(int anio, int mesInicio, int mesFin) {
        return ReporteNomenclatura.nombreDocente(anio, mesInicio, mesFin);
    }

    @Override
    protected void construir(Workbook libro, int anio, int mes) throws SQLException {
        construirDetalleDocente(libro, anio, mes);
    }

    @Override
    protected void construirRango(Workbook libro, int anio, int mesInicio, int mesFin) throws SQLException {
        List<Integer> meses = mesesEnRango(mesInicio, mesFin);
        construirDetalleDocenteRango(libro, anio, meses);
    }

    private void construirDetalleDocente(Workbook libro, int anio, int mes) throws SQLException {
        Sheet hoja = crearHoja(libro, "Detalle Docente");
        List<FilaDocente> filas = service.docenteDetalleDia(anio, mes);
        int dias = diasDelMes(anio, mes);

        Map<Integer, String> nombresDocentes = new LinkedHashMap<>();
        for (FilaDocente f : filas) {
            nombresDocentes.putIfAbsent(f.getDocenteID(), f.getDocente());
        }

        List<String> encabezados = new ArrayList<>(Arrays.asList("Material", "Unidad (base)"));
        boolean[] domingo = new boolean[dias + 3];
        for (int d = 1; d <= dias; d++) {
            encabezados.add(diaConNombre(anio, mes, d));
            domingo[d + 1] = esDomingo(anio, mes, d);
        }
        encabezados.add("Total");

        int filaIndex = 0;
        for (Map.Entry<Integer, String> entry : nombresDocentes.entrySet()) {
            int docenteID = entry.getKey();
            String nombre = entry.getValue();
            seccion(hoja, filaIndex, "Docente: " + nombre, encabezados.size());
            filaIndex++;
            encabezadoDia(hoja, filaIndex, encabezados, domingo);
            filaIndex++;

            Map<Integer, double[]> porMaterial = new LinkedHashMap<>();
            Map<Integer, String[]> meta = new LinkedHashMap<>();
            for (FilaDocente f : filas) {
                if (f.getDocenteID() != docenteID) continue;
                int dia = Integer.parseInt(f.getDia().substring(f.getDia().lastIndexOf('-') + 1));
                double[] valores = porMaterial.computeIfAbsent(f.getMaterialID(), k -> new double[dias]);
                valores[dia - 1] += f.getCantidad();
                meta.putIfAbsent(f.getMaterialID(), new String[]{f.getMaterial(), f.getUnidad()});
            }

            for (Map.Entry<Integer, double[]> me : porMaterial.entrySet()) {
                String[] m = meta.get(me.getKey());
                List<Object> row = new ArrayList<>(Arrays.asList(m));
                double total = 0;
                for (double v : me.getValue()) {
                    row.add(redondear2(v));
                    total += v;
                }
                row.add(redondear2(total));
                fila(hoja, filaIndex++, row);
            }
        }
        autoAjustar(hoja, encabezados.size());
    }

    private void construirDetalleDocenteRango(Workbook libro, int anio, List<Integer> meses) throws SQLException {
        Sheet hoja = crearHoja(libro, "Detalle Docente");

        List<String> encabezados = new ArrayList<>(Arrays.asList("Material", "Unidad (base)"));
        encabezados.addAll(nombresMesesEnRango(meses.get(0), meses.get(meses.size() - 1)));
        encabezados.add("Total");

        int filaIndex = 0;
        Map<String, String> nombresDocentes = new LinkedHashMap<>();

        for (int mes : meses) {
            for (FilaDocente f : service.docenteConsolidado(anio, mes)) {
                nombresDocentes.putIfAbsent(String.valueOf(f.getDocenteID()), f.getDocente());
            }
        }

        for (Map.Entry<String, String> entry : nombresDocentes.entrySet()) {
            int docenteID = Integer.parseInt(entry.getKey());
            String nombre = entry.getValue();

            Map<Integer, double[]> porMaterial = new LinkedHashMap<>();
            Map<Integer, String[]> meta = new LinkedHashMap<>();

            for (int i = 0; i < meses.size(); i++) {
                int mes = meses.get(i);
                for (FilaDocente f : service.docenteConsolidado(anio, mes)) {
                    if (f.getDocenteID() != docenteID) continue;
                    double[] valores = porMaterial.computeIfAbsent(f.getMaterialID(), k -> new double[meses.size()]);
                    valores[i] += f.getCantidad();
                    meta.putIfAbsent(f.getMaterialID(), new String[]{f.getMaterial(), f.getUnidad()});
                }
            }

            if (porMaterial.isEmpty()) continue;

            seccion(hoja, filaIndex, "Docente: " + nombre, encabezados.size());
            filaIndex++;
            encabezado(hoja, filaIndex, encabezados);
            filaIndex++;

            for (Map.Entry<Integer, double[]> me : porMaterial.entrySet()) {
                String[] m = meta.get(me.getKey());
                List<Object> row = new ArrayList<>(Arrays.asList(m));
                double total = 0;
                for (double v : me.getValue()) {
                    row.add(redondear2(v));
                    total += v;
                }
                row.add(redondear2(total));
                fila(hoja, filaIndex++, row);
            }
        }
        autoAjustar(hoja, encabezados.size());
    }
}
