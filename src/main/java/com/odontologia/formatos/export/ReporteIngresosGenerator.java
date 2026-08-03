package com.odontologia.formatos.export;

import com.odontologia.formatos.repository.ReporteRepository.FilaIngreso;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * Reporte de Ingresos (RF-1.7.3, tarea 4.4).
 * Columnas: Grado | Tipo | Cantidad Tratamientos | Ingreso Total | Monto Pagado | Monto Pendiente.
 */
public class ReporteIngresosGenerator extends ReporteGeneradorBase {

    @Override
    protected String nombreArchivo(int anio, int mes) {
        return ReporteNomenclatura.nombre(ReporteNomenclatura.TIPO_INGRESOS, anio, mes);
    }

    @Override
    protected void construir(Workbook libro, int anio, int mes) throws SQLException {
        Sheet hoja = crearHoja(libro, "Ingresos");
        encabezado(hoja, Arrays.asList(
                "Grado", "Tipo", "Cantidad Tratamientos", "Ingreso Total", "Monto Pagado", "Monto Pendiente"));
        List<FilaIngreso> filas = service.ingresos(anio, mes);
        int filaIndex = 1;
        for (FilaIngreso f : filas) {
            fila(hoja, filaIndex++, Arrays.asList(
                    f.getGrado(),
                    f.getTipo(),
                    f.getCantidadTratamientos(),
                    redondear2(f.getIngresoTotal()),
                    redondear2(f.getMontoPagado()),
                    redondear2(f.getMontoPendiente())));
        }
        autoAjustar(hoja, 6);
    }
}
