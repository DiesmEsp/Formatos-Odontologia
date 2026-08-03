package com.odontologia.formatos.export;

import com.odontologia.formatos.repository.ReporteRepository.FilaDocente;
import com.odontologia.formatos.repository.ReporteRepository.FilaEspecialista;
import com.odontologia.formatos.repository.ReporteRepository.FilaIngreso;
import com.odontologia.formatos.repository.ReporteRepository.FilaMaterial;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Reporte Anual (RF-1.7.6, tarea 4.7, RD-3.1.8).
 * Un solo archivo con cuatro hojas (una por tipo de reporte) y el detalle
 * mes por mes durante los 12 meses del año. Los meses sin datos quedan en cero.
 */
public class ReporteAnualGenerator extends ReporteGeneradorBase {

    private static final String[] METRICAS_INGRESOS = {
            "Cantidad Tratamientos", "Ingreso Total", "Monto Pagado", "Monto Pendiente"
    };

    @Override
    protected String nombreArchivo(int anio, int mes) {
        return ReporteNomenclatura.nombreAnual(anio);
    }

    @Override
    protected void construir(Workbook libro, int anio, int mes) throws SQLException {
        construirMateriales(libro, anio);
        construirIngresos(libro, anio);
        construirDocente(libro, anio);
        construirEspecialista(libro, anio);
    }

    private void construirMateriales(Workbook libro, int anio) throws SQLException {
        Sheet hoja = crearHoja(libro, "Materiales");
        encabezado(hoja, encabezadosMensuales("Material", "Unidad (base)"));

        Map<Integer, double[]> porMaterial = new TreeMap<>();
        Map<Integer, String[]> meta = new TreeMap<>();
        for (int mes = 1; mes <= 12; mes++) {
            for (FilaMaterial f : service.materiales(anio, mes)) {
                double[] valores = porMaterial.computeIfAbsent(f.getMaterialID(), k -> new double[12]);
                valores[mes - 1] += f.getCantidadTotal();
                meta.putIfAbsent(f.getMaterialID(), new String[]{f.getNombre(), f.getUnidadBase()});
            }
        }

        int filaIndex = 1;
        for (Map.Entry<Integer, double[]> e : porMaterial.entrySet()) {
            String[] m = meta.get(e.getKey());
            fila(hoja, filaIndex++, filaMensual(new String[]{m[0], m[1]}, e.getValue()));
        }
        autoAjustar(hoja, 15);
    }

    private void construirIngresos(Workbook libro, int anio) throws SQLException {
        Sheet hoja = crearHoja(libro, "Ingresos");
        encabezado(hoja, encabezadosMensuales("Grado", "Tipo", "Métrica"));

        Map<String, double[][]> porGrupo = new LinkedHashMap<>();
        Map<String, String[]> meta = new LinkedHashMap<>();
        for (int mes = 1; mes <= 12; mes++) {
            for (FilaIngreso f : service.ingresos(anio, mes)) {
                String clave = f.getGrado() + "|" + f.getTipo();
                double[][] valores = porGrupo.computeIfAbsent(clave, k -> new double[4][12]);
                valores[0][mes - 1] += f.getCantidadTratamientos();
                valores[1][mes - 1] += f.getIngresoTotal();
                valores[2][mes - 1] += f.getMontoPagado();
                valores[3][mes - 1] += f.getMontoPendiente();
                meta.putIfAbsent(clave, new String[]{f.getGrado(), f.getTipo()});
            }
        }

        int filaIndex = 1;
        for (Map.Entry<String, double[][]> e : porGrupo.entrySet()) {
            String[] m = meta.get(e.getKey());
            double[][] valores = e.getValue();
            for (int metrica = 0; metrica < METRICAS_INGRESOS.length; metrica++) {
                List<Object> fila = new ArrayList<>();
                fila.add(m[0]);
                fila.add(m[1]);
                fila.add(METRICAS_INGRESOS[metrica]);
                agregarMesesYTotal(fila, valores[metrica]);
                fila(hoja, filaIndex++, fila);
            }
        }
        autoAjustar(hoja, 16);
    }

    private void construirDocente(Workbook libro, int anio) throws SQLException {
        Sheet hoja = crearHoja(libro, "Docente");
        encabezado(hoja, encabezadosMensuales("Docente", "Material", "Unidad"));

        Map<String, double[]> porDocenteMaterial = new LinkedHashMap<>();
        Map<String, String[]> meta = new LinkedHashMap<>();
        for (int mes = 1; mes <= 12; mes++) {
            for (FilaDocente f : service.docenteConsolidado(anio, mes)) {
                String clave = f.getDocenteID() + "|" + f.getMaterialID();
                double[] valores = porDocenteMaterial.computeIfAbsent(clave, k -> new double[12]);
                valores[mes - 1] += f.getCantidad();
                meta.putIfAbsent(clave, new String[]{f.getDocente(), f.getMaterial(), f.getUnidad()});
            }
        }

        int filaIndex = 1;
        for (Map.Entry<String, double[]> e : porDocenteMaterial.entrySet()) {
            String[] m = meta.get(e.getKey());
            fila(hoja, filaIndex++, filaMensual(new String[]{m[0], m[1], m[2]}, e.getValue()));
        }
        autoAjustar(hoja, 16);
    }

    private void construirEspecialista(Workbook libro, int anio) throws SQLException {
        Sheet hoja = crearHoja(libro, "Especialista");
        encabezado(hoja, encabezadosMensuales("Especialista", "Grado", "Tipo", "Material", "Unidad"));

        Map<String, double[]> porEspecialistaMaterial = new LinkedHashMap<>();
        Map<String, String[]> meta = new LinkedHashMap<>();
        for (int mes = 1; mes <= 12; mes++) {
            for (FilaEspecialista f : service.especialista(anio, mes)) {
                String clave = f.getOperadorID() + "|" + f.getMaterialID();
                double[] valores = porEspecialistaMaterial.computeIfAbsent(clave, k -> new double[12]);
                valores[mes - 1] += f.getCantidad();
                meta.putIfAbsent(clave,
                        new String[]{f.getEspecialista(), f.getGrado(), f.getTipo(), f.getMaterial(), f.getUnidad()});
            }
        }

        int filaIndex = 1;
        for (Map.Entry<String, double[]> e : porEspecialistaMaterial.entrySet()) {
            String[] m = meta.get(e.getKey());
            fila(hoja, filaIndex++, filaMensual(m, e.getValue()));
        }
        autoAjustar(hoja, 18);
    }

    private List<String> encabezadosMensuales(String... fijas) {
        List<String> encabezados = new ArrayList<>();
        encabezados.addAll(List.of(fijas));
        encabezados.addAll(mesesAnio());
        encabezados.add("Total");
        return encabezados;
    }

    private List<Object> filaMensual(String[] fijos, double[] valores) {
        List<Object> fila = new ArrayList<>();
        fila.addAll(List.of(fijos));
        agregarMesesYTotal(fila, valores);
        return fila;
    }

    private void agregarMesesYTotal(List<Object> fila, double[] valores) {
        double total = 0;
        for (int i = 0; i < 12; i++) {
            fila.add(redondear2(valores[i]));
            total += valores[i];
        }
        fila.add(redondear2(total));
    }
}
