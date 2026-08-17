package com.odontologia.formatos.export;

import com.odontologia.formatos.repository.ReporteRepository;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.sql.SQLException;
import java.util.*;

public class ReporteConsolidadoGenerator extends ReporteGeneradorBase {

    private static final String TIPO = "Consolidado";

    @Override
    protected String nombreArchivo(int anio, int mes) {
        return TIPO + "_" + ReporteNomenclatura.mes(mes) + "_" + anio + ".xlsx";
    }

    @Override
    protected String nombreArchivoRango(int anio, int mesInicio, int mesFin) {
        if (mesInicio == 1 && mesFin == 12) {
            return TIPO + "_" + anio + ".xlsx";
        }
        return TIPO + "_" + ReporteNomenclatura.mesCorto(mesInicio) + "_"
                + ReporteNomenclatura.mesCorto(mesFin) + "_" + anio + ".xlsx";
    }

    @Override
    protected void construir(Workbook libro, int anio, int mes) throws SQLException {
        construirRango(libro, anio, mes, mes);
    }

    @Override
    protected void construirRango(Workbook libro, int anio, int mesInicio, int mesFin) throws SQLException {
        List<Integer> meses = mesesEnRango(mesInicio, mesFin);
        construirHojaMateriales(libro, anio, meses);
        construirHojaIngresos(libro, anio, meses);
        construirHojaAsistencia(libro, anio, meses);
    }

    private void construirHojaMateriales(Workbook libro, int anio, List<Integer> meses) throws SQLException {
        Sheet hoja = crearHoja(libro, "Materiales");

        List<String> encs = new ArrayList<>();
        encs.add("Material");
        encs.add("Unidad");
        for (int m : meses) encs.add(ReporteNomenclatura.mesCorto(m));
        encs.add("Total");
        encabezado(hoja, encs);

        Map<String, double[]> porMaterial = new LinkedHashMap<>();
        Map<String, String> unidades = new LinkedHashMap<>();

        for (int i = 0; i < meses.size(); i++) {
            for (ReporteRepository.FilaMaterial f : service.materiales(anio, meses.get(i), clinicaID())) {
                double[] vals = porMaterial.computeIfAbsent(f.getNombre(), k -> new double[meses.size()]);
                vals[i] += f.getCantidadTotal();
                unidades.putIfAbsent(f.getNombre(), f.getUnidadBase());
            }
        }

        int filaIdx = 1;
        for (Map.Entry<String, double[]> e : porMaterial.entrySet()) {
            List<Object> fila = new ArrayList<>();
            fila.add(e.getKey());
            fila.add(unidades.getOrDefault(e.getKey(), ""));
            double total = 0;
            for (double v : e.getValue()) {
                fila.add(redondear2(v));
                total += v;
            }
            fila.add(redondear2(total));
            fila(hoja, filaIdx++, fila);
        }

        autoAjustar(hoja, encs.size());
    }

    private void construirHojaIngresos(Workbook libro, int anio, List<Integer> meses) throws SQLException {
        Sheet hoja = crearHoja(libro, "Ingresos");

        List<String> encs = new ArrayList<>();
        encs.add("Tratamiento");
        for (int m : meses) encs.add(ReporteNomenclatura.mesCorto(m));
        encs.add("Total");
        encabezado(hoja, encs);

        Map<String, double[]> porTratamiento = new LinkedHashMap<>();

        for (int i = 0; i < meses.size(); i++) {
            for (ReporteRepository.FilaIngresoTratamiento f : service.ingresosPorTratamiento(anio, meses.get(i), clinicaID())) {
                double[] vals = porTratamiento.computeIfAbsent(f.getTratamiento(), k -> new double[meses.size()]);
                vals[i] += f.getIngresoTotal();
            }
        }

        int filaIdx = 1;
        for (Map.Entry<String, double[]> e : porTratamiento.entrySet()) {
            List<Object> fila = new ArrayList<>();
            fila.add(e.getKey());
            double total = 0;
            for (double v : e.getValue()) {
                fila.add(redondear2(v));
                total += v;
            }
            fila.add(redondear2(total));
            fila(hoja, filaIdx++, fila);
        }

        autoAjustar(hoja, encs.size());
    }

    private void construirHojaAsistencia(Workbook libro, int anio, List<Integer> meses) throws SQLException {
        Sheet hoja = crearHoja(libro, "Asistencia");

        List<String> encs = new ArrayList<>();
        encs.add("Docente");
        for (int m : meses) encs.add(ReporteNomenclatura.mesCorto(m));
        encs.add("Total");
        encabezado(hoja, encs);

        Map<String, int[]> porDocente = new LinkedHashMap<>();

        for (int i = 0; i < meses.size(); i++) {
            for (ReporteRepository.FilaAsistencia f : service.datosAsistencia(anio, meses.get(i), clinicaID())) {
                int[] presencias = porDocente.computeIfAbsent(f.getDocente(), k -> new int[meses.size()]);
                if (f.getHoraEntrada() != null && !f.getHoraEntrada().isEmpty()) {
                    presencias[i]++;
                }
            }
        }

        int filaIdx = 1;
        for (Map.Entry<String, int[]> e : porDocente.entrySet()) {
            List<Object> fila = new ArrayList<>();
            fila.add(e.getKey());
            int total = 0;
            for (int v : e.getValue()) {
                fila.add(v);
                total += v;
            }
            fila.add(total);
            fila(hoja, filaIdx++, fila);
        }

        autoAjustar(hoja, encs.size());
    }
}
