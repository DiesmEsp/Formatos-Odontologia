package com.odontologia.formatos.export;

import com.odontologia.formatos.repository.ReporteRepository.FilaIngresoOperador;
import com.odontologia.formatos.repository.ReporteRepository.FilaIngresoTratamiento;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reporte Económico unificado.
 * <p>
 * 2 hojas: General (por tratamiento, toda la clínica),
 * Por Operador (tablas por operador con Tratamiento | Mes... | Total).
 * No incluye docente — la asistencia docente no genera ingresos.
 */
public class ReporteEconomicoGenerator extends ReporteGeneradorBase {

    @Override
    protected String nombreArchivo(int anio, int mes) {
        return ReporteNomenclatura.nombreEconomico(anio, mes);
    }

    @Override
    protected String nombreArchivoRango(int anio, int mesInicio, int mesFin) {
        return ReporteNomenclatura.nombreEconomico(anio, mesInicio, mesFin);
    }

    @Override
    protected void construir(Workbook libro, int anio, int mes) throws SQLException {
        construirGeneral(libro, anio, mes);
        construirPorOperador(libro, anio, mes);
    }

    @Override
    protected void construirRango(Workbook libro, int anio, int mesInicio, int mesFin) throws SQLException {
        List<Integer> meses = mesesEnRango(mesInicio, mesFin);
        construirGeneralRango(libro, anio, meses);
        construirPorOperadorRango(libro, anio, meses);
    }

    private void construirGeneral(Workbook libro, int anio, int mes) throws SQLException {
        Sheet hoja = crearHoja(libro, "General");
        encabezado(hoja, Arrays.asList(
                "Tratamiento", "Cantidad Tratamientos", "Ingreso Total", "Monto Pagado", "Monto Pendiente"));
        List<FilaIngresoTratamiento> filas = service.ingresosPorTratamiento(anio, mes, clinicaID());
        int filaIndex = 1;
        for (FilaIngresoTratamiento f : filas) {
            fila(hoja, filaIndex++, Arrays.asList(
                    f.getTratamiento(),
                    f.getCantidadTratamientos(),
                    redondear2(f.getIngresoTotal()),
                    redondear2(f.getMontoPagado()),
                    redondear2(f.getMontoPendiente())));
        }
        autoAjustar(hoja, 5);
    }

    private void construirGeneralRango(Workbook libro, int anio, List<Integer> meses) throws SQLException {
        Sheet hoja = crearHoja(libro, "General");
        List<String> encabezados = new ArrayList<>(Arrays.asList("Tratamiento"));
        encabezados.addAll(nombresMesesEnRango(meses.get(0), meses.get(meses.size() - 1)));
        encabezados.add("Total");
        encabezado(hoja, encabezados);

        Map<String, double[]> porTratamiento = new LinkedHashMap<>();
        for (int i = 0; i < meses.size(); i++) {
            int mes = meses.get(i);
            for (FilaIngresoTratamiento f : service.ingresosPorTratamiento(anio, mes, clinicaID())) {
                double[] valores = porTratamiento.computeIfAbsent(f.getTratamiento(), k -> new double[meses.size()]);
                valores[i] += f.getIngresoTotal();
            }
        }

        int filaIndex = 1;
        for (Map.Entry<String, double[]> e : porTratamiento.entrySet()) {
            List<Object> fila = new ArrayList<>();
            fila.add(e.getKey());
            agregarMesesYTotal(fila, e.getValue());
            fila(hoja, filaIndex++, fila);
        }
        autoAjustar(hoja, encabezados.size());
    }

    private void construirPorOperador(Workbook libro, int anio, int mes) throws SQLException {
        Sheet hoja = crearHoja(libro, "Por Operador");
        List<FilaIngresoOperador> filas = service.ingresosPorOperador(anio, mes, clinicaID());

        Map<Integer, String[]> nombresOperadores = new LinkedHashMap<>();
        for (FilaIngresoOperador f : filas) {
            nombresOperadores.putIfAbsent(f.getOperadorID(),
                    new String[]{f.getNombre(), f.getGrado(), f.getTipo()});
        }

        List<String> encabezados = Arrays.asList(
                "Tratamiento", "Cantidad", "Ingreso Total", "Monto Pagado", "Monto Pendiente");

        int filaIndex = 0;
        for (Map.Entry<Integer, String[]> entry : nombresOperadores.entrySet()) {
            int operadorID = entry.getKey();
            String[] info = entry.getValue();
            seccion(hoja, filaIndex,
                    "Operador: " + info[0] + " (" + info[1] + "-" + info[2] + ")", encabezados.size());
            filaIndex++;
            encabezado(hoja, filaIndex, encabezados);
            filaIndex++;

            for (FilaIngresoOperador f : filas) {
                if (f.getOperadorID() != operadorID) continue;
                fila(hoja, filaIndex++, Arrays.asList(
                        f.getTratamiento(),
                        f.getCantidad(),
                        redondear2(f.getIngresoTotal()),
                        redondear2(f.getMontoPagado()),
                        redondear2(f.getMontoPendiente())));
            }
        }
        autoAjustar(hoja, encabezados.size());
    }

    private void construirPorOperadorRango(Workbook libro, int anio, List<Integer> meses) throws SQLException {
        Sheet hoja = crearHoja(libro, "Por Operador");

        List<String> encabezados = new ArrayList<>(Arrays.asList("Tratamiento"));
        encabezados.addAll(nombresMesesEnRango(meses.get(0), meses.get(meses.size() - 1)));
        encabezados.add("Total");

        int filaIndex = 0;
        Map<Integer, String[]> nombresOperadores = new LinkedHashMap<>();

        for (int i = 0; i < meses.size(); i++) {
            int mes = meses.get(i);
            for (FilaIngresoOperador f : service.ingresosPorOperador(anio, mes, clinicaID())) {
                nombresOperadores.putIfAbsent(f.getOperadorID(),
                        new String[]{f.getNombre(), f.getGrado(), f.getTipo()});
            }
        }

        for (Map.Entry<Integer, String[]> entry : nombresOperadores.entrySet()) {
            int operadorID = entry.getKey();
            String[] info = entry.getValue();

            Map<String, double[]> porTratamiento = new LinkedHashMap<>();

            for (int i = 0; i < meses.size(); i++) {
                int mes = meses.get(i);
                for (FilaIngresoOperador f : service.ingresosPorOperador(anio, mes, clinicaID())) {
                    if (f.getOperadorID() != operadorID) continue;
                    double[] valores = porTratamiento.computeIfAbsent(f.getTratamiento(), k -> new double[meses.size()]);
                    valores[i] += f.getIngresoTotal();
                }
            }

            if (porTratamiento.isEmpty()) continue;

            seccion(hoja, filaIndex,
                    "Operador: " + info[0] + " (" + info[1] + "-" + info[2] + ")", encabezados.size());
            filaIndex++;
            encabezado(hoja, filaIndex, encabezados);
            filaIndex++;

            for (Map.Entry<String, double[]> me : porTratamiento.entrySet()) {
                List<Object> fila = new ArrayList<>();
                fila.add(me.getKey());
                agregarMesesYTotal(fila, me.getValue());
                fila(hoja, filaIndex++, fila);
            }
        }
        autoAjustar(hoja, encabezados.size());
    }

    private void agregarMesesYTotal(List<Object> fila, double[] valores) {
        double total = 0;
        for (double v : valores) {
            fila.add(redondear2(v));
            total += v;
        }
        fila.add(redondear2(total));
    }
}