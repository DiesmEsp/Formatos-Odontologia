package com.odontologia.formatos.export;

import com.odontologia.formatos.repository.ReporteRepository.FilaMaterial;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * Reporte General de Materiales (RF-1.7.2, tarea 4.3).
 * Columnas: Material | Unidad (base) | Cantidad Total.
 * El consumo ya viene convertido a la unidad base (factor 1 si no hay conversión).
 */
public class ReporteMaterialesGenerator extends ReporteGeneradorBase {

    @Override
    protected String nombreArchivo(int anio, int mes) {
        return ReporteNomenclatura.nombre(ReporteNomenclatura.TIPO_MATERIALES, anio, mes);
    }

    @Override
    protected void construir(Workbook libro, int anio, int mes) throws SQLException {
        Sheet hoja = crearHoja(libro, "Materiales");
        encabezado(hoja, Arrays.asList("Material", "Unidad (base)", "Cantidad Total"));
        List<FilaMaterial> filas = service.materiales(anio, mes);
        int filaIndex = 1;
        for (FilaMaterial f : filas) {
            fila(hoja, filaIndex++, Arrays.asList(
                    f.getNombre(), f.getUnidadBase(), redondear2(f.getCantidadTotal())));
        }
        autoAjustar(hoja, 3);
    }
}
