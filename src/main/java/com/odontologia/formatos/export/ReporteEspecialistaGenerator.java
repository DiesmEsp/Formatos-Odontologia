package com.odontologia.formatos.export;

import com.odontologia.formatos.repository.ReporteRepository.FilaEspecialista;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reporte de consumo de materiales por especialista (operador).
 * 1 hoja con tablas por operador desglosando material y cantidad.
 */
public class ReporteEspecialistaGenerator extends ReporteGeneradorBase {

    @Override
    protected String nombreArchivo(int anio, int mes) {
        return ReporteNomenclatura.nombreEspecialista(anio, mes);
    }

    @Override
    protected String nombreArchivoRango(int anio, int mesInicio, int mesFin) {
        return ReporteNomenclatura.nombreEspecialista(anio, mesInicio, mesFin);
    }

    @Override
    protected void construir(Workbook libro, int anio, int mes) throws SQLException {
        construirPorOperador(libro, anio, mes);
    }

    @Override
    protected void construirRango(Workbook libro, int anio, int mesInicio, int mesFin) throws SQLException {
        List<Integer> meses = mesesEnRango(mesInicio, mesFin);
        construirPorOperadorRango(libro, anio, meses);
    }

    private void construirPorOperador(Workbook libro, int anio, int mes) throws SQLException {
        Sheet hoja = crearHoja(libro, "Por Operador");
        List<FilaEspecialista> filas = service.especialista(anio, mes);

        Map<Integer, String[]> nombresOperadores = new LinkedHashMap<>();
        for (FilaEspecialista f : filas) {
            nombresOperadores.putIfAbsent(f.getOperadorID(),
                    new String[]{f.getEspecialista(), f.getGrado(), f.getTipo()});
        }

        List<String> encabezados = Arrays.asList("Material", "Unidad (base)", "Cantidad Total");

        int filaIndex = 0;
        for (Map.Entry<Integer, String[]> entry : nombresOperadores.entrySet()) {
            int operadorID = entry.getKey();
            String[] info = entry.getValue();
            seccion(hoja, filaIndex,
                    "Operador: " + info[0] + " (" + info[1] + "-" + info[2] + ")", encabezados.size());
            filaIndex++;
            encabezado(hoja, filaIndex, encabezados);
            filaIndex++;

            for (FilaEspecialista f : filas) {
                if (f.getOperadorID() != operadorID) continue;
                fila(hoja, filaIndex++, Arrays.asList(
                        f.getMaterial(), f.getUnidad(), redondear2(f.getCantidad())));
            }
        }
        autoAjustar(hoja, encabezados.size());
    }

    private void construirPorOperadorRango(Workbook libro, int anio, List<Integer> meses) throws SQLException {
        Sheet hoja = crearHoja(libro, "Por Operador");

        List<String> encabezados = new ArrayList<>(Arrays.asList("Material", "Unidad (base)"));
        encabezados.addAll(nombresMesesEnRango(meses.get(0), meses.get(meses.size() - 1)));
        encabezados.add("Total");

        int filaIndex = 0;
        Map<Integer, String[]> nombresOperadores = new LinkedHashMap<>();

        for (int mes : meses) {
            for (FilaEspecialista f : service.especialista(anio, mes)) {
                nombresOperadores.putIfAbsent(f.getOperadorID(),
                        new String[]{f.getEspecialista(), f.getGrado(), f.getTipo()});
            }
        }

        for (Map.Entry<Integer, String[]> entry : nombresOperadores.entrySet()) {
            int operadorID = entry.getKey();
            String[] info = entry.getValue();

            Map<Integer, double[]> porMaterial = new LinkedHashMap<>();
            Map<Integer, String[]> meta = new LinkedHashMap<>();

            for (int i = 0; i < meses.size(); i++) {
                int mes = meses.get(i);
                for (FilaEspecialista f : service.especialista(anio, mes)) {
                    if (f.getOperadorID() != operadorID) continue;
                    double[] valores = porMaterial.computeIfAbsent(f.getMaterialID(), k -> new double[meses.size()]);
                    valores[i] += f.getCantidad();
                    meta.putIfAbsent(f.getMaterialID(), new String[]{f.getMaterial(), f.getUnidad()});
                }
            }

            if (porMaterial.isEmpty()) continue;

            seccion(hoja, filaIndex,
                    "Operador: " + info[0] + " (" + info[1] + "-" + info[2] + ")", encabezados.size());
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
