package com.odontologia.formatos.export;

import com.odontologia.formatos.repository.ReporteRepository.FilaTratamiento;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reporte de consumo de materiales por tratamiento.
 * Agrupa por operador y, dentro de cada operador, por tratamiento,
 * detallando los materiales y cantidades consumidas.
 */
public class ReporteTratamientoGenerator extends ReporteGeneradorBase {

    private Integer operadorID;
    private String tipo;

    public void setOperadorID(Integer operadorID) {
        this.operadorID = operadorID;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    protected String nombreArchivo(int anio, int mes) {
        return ReporteNomenclatura.nombreTratamiento(anio, mes);
    }

    @Override
    protected void construir(Workbook libro, int anio, int mes) throws SQLException {
        Sheet hoja = crearHoja(libro, "Por Tratamiento");
        List<FilaTratamiento> filas = service.consumoPorTratamiento(anio, mes, operadorID, tipo, clinicaID());

        titulo(hoja, "Consumo de Materiales por Tratamiento — " + ReporteNomenclatura.mes(mes) + " " + anio, 5);

        List<String> encabezados = Arrays.asList("Material", "Unidad (base)", "Cantidad");

        Map<String, List<FilaTratamiento>> porOperador = new LinkedHashMap<>();
        for (FilaTratamiento f : filas) {
            porOperador.computeIfAbsent(f.getOperador(), k -> new ArrayList<>()).add(f);
        }

        int filaIndex = 1;
        for (Map.Entry<String, List<FilaTratamiento>> e : porOperador.entrySet()) {
            FilaTratamiento primero = e.getValue().get(0);
            seccion(hoja, filaIndex,
                    "Operador: " + e.getKey() + " (" + primero.getGrado() + "-" + primero.getTipo() + ")",
                    encabezados.size());
            filaIndex++;

            Map<Integer, List<FilaTratamiento>> porTratamiento = new LinkedHashMap<>();
            for (FilaTratamiento f : e.getValue()) {
                porTratamiento.computeIfAbsent(f.getTratamientoID(), k -> new ArrayList<>()).add(f);
            }

            for (Map.Entry<Integer, List<FilaTratamiento>> te : porTratamiento.entrySet()) {
                FilaTratamiento t = te.getValue().get(0);
                seccion(hoja, filaIndex,
                        "Tratamiento #" + t.getTratamientoID() + ": " + t.getNombreTratamiento() + " (" + t.getFecha() + ")",
                        encabezados.size());
                filaIndex++;
                encabezado(hoja, filaIndex, encabezados);
                filaIndex++;

                for (FilaTratamiento m : te.getValue()) {
                    fila(hoja, filaIndex++, Arrays.asList(
                            m.getMaterial(), m.getUnidad(), redondear2(m.getCantidad())));
                }
            }
        }

        autoAjustar(hoja, encabezados.size());
    }
}
