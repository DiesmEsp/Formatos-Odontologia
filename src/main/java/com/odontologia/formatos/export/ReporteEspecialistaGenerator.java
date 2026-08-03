package com.odontologia.formatos.export;

import com.odontologia.formatos.repository.ReporteRepository.FilaEspecialista;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * Reporte de Consumo Especialista (RF-1.7.5, tarea 4.6).
 * Columnas: Especialista | Grado | Tipo | Material | Unidad | Cantidad.
 */
public class ReporteEspecialistaGenerator extends ReporteGeneradorBase {

    @Override
    protected String nombreArchivo(int anio, int mes) {
        return ReporteNomenclatura.nombre(ReporteNomenclatura.TIPO_ESPECIALISTA, anio, mes);
    }

    @Override
    protected void construir(Workbook libro, int anio, int mes) throws SQLException {
        Sheet hoja = crearHoja(libro, "Especialista");
        encabezado(hoja, Arrays.asList("Especialista", "Grado", "Tipo", "Material", "Unidad", "Cantidad"));
        List<FilaEspecialista> filas = service.especialista(anio, mes);
        int filaIndex = 1;
        for (FilaEspecialista f : filas) {
            fila(hoja, filaIndex++, Arrays.asList(
                    f.getEspecialista(),
                    f.getGrado(),
                    f.getTipo(),
                    f.getMaterial(),
                    f.getUnidad(),
                    redondear2(f.getCantidad())));
        }
        autoAjustar(hoja, 6);
    }
}
