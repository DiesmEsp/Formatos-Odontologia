package com.odontologia.formatos.export;

import com.odontologia.formatos.repository.ReporteRepository.FilaDocente;
import com.odontologia.formatos.repository.ReporteRepository.FilaEspecialista;
import com.odontologia.formatos.repository.ReporteRepository.FilaMaterial;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Reporte de Materiales unificado.
 * <p>
 * 3 hojas: General (toda la clínica), Detalle Docente (tablas por docente),
 * Por Operador (tablas por operador con Material | Unidad | Mes... | Total).
 */
public class ReporteMaterialesGenerator extends ReporteGeneradorBase {

    @Override
    protected String nombreArchivo(int anio, int mes) {
        return ReporteNomenclatura.nombreMateriales(anio, mes);
    }

    @Override
    protected String nombreArchivoRango(int anio, int mesInicio, int mesFin) {
        return ReporteNomenclatura.nombreMateriales(anio, mesInicio, mesFin);
    }

    @Override
    protected void construir(Workbook libro, int anio, int mes) throws SQLException {
        construirGeneral(libro, anio, mes);
        construirDetalleDocente(libro, anio, mes);
        construirPorOperador(libro, anio, mes);
    }

    @Override
    protected void construirRango(Workbook libro, int anio, int mesInicio, int mesFin) throws SQLException {
        List<Integer> meses = mesesEnRango(mesInicio, mesFin);
        construirGeneralRango(libro, anio, meses);
        construirDetalleDocenteRango(libro, anio, meses);
        construirPorOperadorRango(libro, anio, meses);
    }

    private void construirGeneral(Workbook libro, int anio, int mes) throws SQLException {
        Sheet hoja = crearHoja(libro, "General");
        encabezado(hoja, Arrays.asList("Material", "Unidad (base)", "Cantidad Total"));
        List<FilaMaterial> filas = service.materiales(anio, mes, clinicaID());
        int filaIndex = 1;
        for (FilaMaterial f : filas) {
            fila(hoja, filaIndex++, Arrays.asList(
                    f.getNombre(), f.getUnidadBase(), redondear2(f.getCantidadTotal())));
        }
        autoAjustar(hoja, 3);
    }

    private void construirGeneralRango(Workbook libro, int anio, List<Integer> meses) throws SQLException {
        Sheet hoja = crearHoja(libro, "General");
        List<String> encabezados = new ArrayList<>(Arrays.asList("Material", "Unidad (base)"));
        encabezados.addAll(nombresMesesEnRango(meses.get(0), meses.get(meses.size() - 1)));
        encabezados.add("Total");
        encabezado(hoja, encabezados);

        Map<Integer, double[]> porMaterial = new TreeMap<>();
        Map<Integer, String[]> meta = new TreeMap<>();
        for (int i = 0; i < meses.size(); i++) {
            int mes = meses.get(i);
            for (FilaMaterial f : service.materiales(anio, mes, clinicaID())) {
                double[] valores = porMaterial.computeIfAbsent(f.getMaterialID(), k -> new double[meses.size()]);
                valores[i] += f.getCantidadTotal();
                meta.putIfAbsent(f.getMaterialID(), new String[]{f.getNombre(), f.getUnidadBase()});
            }
        }

        int filaIndex = 1;
        for (Map.Entry<Integer, double[]> e : porMaterial.entrySet()) {
            String[] m = meta.get(e.getKey());
            List<Object> fila = new ArrayList<>(Arrays.asList(m[0], m[1]));
            agregarMesesYTotal(fila, e.getValue());
            fila(hoja, filaIndex++, fila);
        }
        autoAjustar(hoja, encabezados.size());
    }

    private void construirDetalleDocente(Workbook libro, int anio, int mes) throws SQLException {
        Sheet hoja = crearHoja(libro, "Detalle Docente");
        List<FilaDocente> filas = service.docenteDetalleDia(anio, mes, clinicaID());
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
                List<Object> fila = new ArrayList<>(Arrays.asList(m));
                agregarMesesYTotal(fila, me.getValue());
                fila(hoja, filaIndex++, fila);
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

        for (int i = 0; i < meses.size(); i++) {
            int mes = meses.get(i);
            for (FilaDocente f : service.docenteConsolidado(anio, mes, clinicaID())) {
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
                for (FilaDocente f : service.docenteConsolidado(anio, mes, clinicaID())) {
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
                List<Object> fila = new ArrayList<>(Arrays.asList(m));
                agregarMesesYTotal(fila, me.getValue());
                fila(hoja, filaIndex++, fila);
            }
        }
        autoAjustar(hoja, encabezados.size());
    }

    private void construirPorOperador(Workbook libro, int anio, int mes) throws SQLException {
        Sheet hoja = crearHoja(libro, "Por Operador");
        List<FilaEspecialista> filas = service.especialista(anio, mes, clinicaID());

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

        for (int i = 0; i < meses.size(); i++) {
            int mes = meses.get(i);
            for (FilaEspecialista f : service.especialista(anio, mes, clinicaID())) {
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
                for (FilaEspecialista f : service.especialista(anio, mes, clinicaID())) {
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
                List<Object> fila = new ArrayList<>(Arrays.asList(m));
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